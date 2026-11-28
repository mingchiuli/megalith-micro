use anyhow::{Result, ensure};
use megalith_api::client::BaseClient;
use megalith_service_runtime::tasks::BackgroundTasks;
use megalith_service_runtime::{messaging, settings::Settings, web};
use micro_auth_rs::{
    adapter::{
        inbound::http,
        outbound::{
            codes::CodeStore,
            delivery::Delivery,
            directory::{CONTRACTS, Directory},
        },
    },
    application::{Auth, CookiePolicy},
    domain::token::Tokens,
};
use std::{sync::Arc, time::Duration};

fn main() -> Result<()> {
    let s = Settings::load(include_str!("../application.yml"))?;
    let port = s.parse("server.port")?;
    web::run("micro-auth", port, async move {
        let tasks = BackgroundTasks::default();
        let http = reqwest::Client::builder()
            .timeout(Duration::from_secs(10))
            .build()?;
        let redis = s.redis().await?;
        let cache = s.cache(redis.clone(), &CONTRACTS).await?;
        let directory = Directory {
            base: BaseClient::new(http.clone(), &s.get("megalith.blog.user-url")?),
            cache,
        };
        let tokens = Tokens::new(
            &s.get("megalith.auth.jwt.secret")?,
            s.get("megalith.auth.jwt.issuer")?,
            s.get("megalith.auth.jwt.audience")?,
            s.parse("megalith.auth.jwt.access-token-expire")?,
            s.parse("megalith.auth.jwt.refresh-token-expire")?,
            s.parse("megalith.auth.jwt.websocket-token-expire")?,
        )?;
        let codes = CodeStore {
            redis,
            attempts: s.parse("megalith.auth.code.max-attempts")?,
            password_window_ms: s
                .duration(
                    "megalith.auth.password-failures.window",
                    Duration::from_secs(900),
                )?
                .as_millis()
                .try_into()?,
            password_attempts: s.parse("megalith.auth.password-failures.max-attempts")?,
        };
        ensure!(
            codes.attempts > 0 && codes.password_attempts > 0,
            "invalid attempt limits"
        );
        let cookies = CookiePolicy {
            path: s.get("megalith.auth.cookie.path")?,
            secure: s.parse("megalith.auth.cookie.secure")?,
            same_site: s.get("megalith.auth.cookie.same-site")?,
        };
        ensure!(
            cookies.path.starts_with('/') && !cookies.same_site.trim().is_empty(),
            "invalid token cookie policy"
        );
        let delivery = Delivery::new(&s, http)?;
        let events = Arc::new(directory.clone());
        let worker = messaging::consume(
            s.amqp()?,
            "user.auth.menu.change.queue.auth",
            "user.auth.menu.change.fanout.exchange",
            move |bytes| {
                let app = events.clone();
                async move { app.evict(&serde_json::from_slice(&bytes)?).await }
            },
        );
        let health = directory.cache.clone();
        let result = web::serve(
            port,
            http::router(Auth {
                tasks: tasks.clone(),
                directory: Arc::new(directory),
                tokens,
                codes: Arc::new(codes),
                delivery: Arc::new(delivery),
                cookies,
            }),
            health.clone(),
        )
        .await;
        tokio::join!(worker.shutdown(), tasks.shutdown());
        if let Some(cache) = Arc::into_inner(health) {
            cache.shutdown().await?;
        }
        result
    })
}
