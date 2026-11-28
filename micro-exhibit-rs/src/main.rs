use anyhow::{Context, Result, ensure};
use megalith_api::client::BaseClient;
use megalith_service_runtime::tasks::BackgroundTasks;
use megalith_service_runtime::{messaging, settings::Settings, web};
use micro_exhibit_rs::{
    adapter::{
        inbound::http,
        outbound::{
            directory::{BlogDirectory, CONTENT},
            state::{Change, StateStore},
        },
    },
    application::Exhibit,
};
use std::time::Duration;

fn main() -> Result<()> {
    let settings = Settings::load(include_str!("../application.yml"))?;
    let port = settings.parse("server.port")?;
    web::run("micro-exhibit", port, async move {
        let s = settings;
        let tasks = BackgroundTasks::default();
        let http = reqwest::Client::builder()
            .timeout(Duration::from_secs(10))
            .build()?;
        let redis = s.redis().await?;
        let cache = s.cache(redis.clone(), &[CONTENT]).await?;
        let blogs = BlogDirectory {
            base: BaseClient::new(http.clone(), &s.get("megalith.blog.blog-url")?),
            cache,
        };
        let store = StateStore { redis };
        let app = Exhibit {
            tasks: tasks.clone(),
            blogs: std::sync::Arc::new(blogs.clone()),
            store: std::sync::Arc::new(store.clone()),
            recommendations: std::sync::Arc::new(
                micro_exhibit_rs::adapter::outbound::search::SearchReader {
                    http,
                    search: s.get("megalith.blog.search-url")?,
                    page_size: s.parse("megalith.blog.blog-page-size")?,
                },
            ),
        };
        let event_store = store.clone();
        let event_blogs = blogs.clone();
        let consumer = messaging::consume(
            s.amqp()?,
            "blog.change.queue.cache",
            "blog.change.fanout.exchange",
            move |bytes| {
                let store = event_store.clone();
                let blogs = event_blogs.clone();
                async move {
                    let event: Change = serde_json::from_slice(&bytes)?;
                    store.apply(&event).await?;
                    blogs
                        .evict_content(
                            event.blog_snapshot.id.context("missing blog id")?,
                            event.blog_snapshot.published_revision_id,
                            event.previous_published_revision_id,
                        )
                        .await
                }
            },
        );
        let batch = s.parse("megalith.blog.existence-index.batch-size")?;
        ensure!(
            (1..=1000).contains(&batch),
            "invalid existence index batch size"
        );
        let delay = s.duration(
            "megalith.blog.existence-index.reconcile-delay",
            Duration::from_secs(30),
        )?;
        let initial = s.delay(
            "megalith.blog.existence-index.initial-delay",
            Duration::ZERO,
        )?;
        let maintenance = store.clone();
        let catalog = blogs.base.clone();
        let rebuild = tokio::spawn(async move {
            tokio::time::sleep(initial).await;
            loop {
                if let Err(error) = maintenance.rebuild(&catalog, batch).await {
                    tracing::warn!(%error,"existence rebuild failed");
                }
                tokio::time::sleep(delay).await;
            }
        });
        let statistics = store;
        let rollover = tokio::spawn(async move {
            use chrono::{Local, TimeZone};
            loop {
                let now = Local::now();
                let Some(midnight) = now
                    .date_naive()
                    .succ_opt()
                    .and_then(|d| d.and_hms_opt(0, 0, 0))
                    .and_then(|d| Local.from_local_datetime(&d).earliest())
                else {
                    tokio::time::sleep(Duration::from_secs(3600)).await;
                    continue;
                };
                tokio::time::sleep((midnight - now).to_std().unwrap_or_default()).await;
                if let Err(error) = statistics.rollover().await {
                    tracing::warn!(%error,"visit rollover failed");
                }
            }
        });
        let health = blogs.cache;
        let result = web::serve(port, http::router(app), health.clone()).await;
        rebuild.abort();
        rollover.abort();
        let _ = tokio::join!(consumer.shutdown(), tasks.shutdown(), rebuild, rollover);
        if let Some(cache) = std::sync::Arc::into_inner(health) {
            cache.shutdown().await?;
        }
        result
    })
}
