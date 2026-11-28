use anyhow::{Result, ensure};
use axum::{
    Json, Router,
    extract::Request,
    http::{HeaderMap, StatusCode},
    middleware::{Next, from_fn},
    response::{IntoResponse, Response},
    routing::get,
};
use base64::{
    Engine as _,
    engine::general_purpose::{URL_SAFE, URL_SAFE_NO_PAD},
};
use megalith_api::{RpcResult, auth::AuthPrincipal, client::RpcError};
use serde::Serialize;
use tracing::Instrument;

pub fn principal(headers: &HeaderMap) -> Result<AuthPrincipal, WebError> {
    let invalid = || WebError::validation("invalid internal principal");
    let value = headers
        .get("x-megalith-principal")
        .map(|h| h.to_str().map_err(|_| invalid()))
        .transpose()?
        .unwrap_or("");
    if value.trim().is_empty() {
        return Ok(AuthPrincipal {
            user_id: 0,
            roles: Vec::new(),
            data_permissions: Vec::new(),
        });
    }
    let bytes = URL_SAFE_NO_PAD
        .decode(value)
        .or_else(|_| URL_SAFE.decode(value))
        .map_err(|_| invalid())?;
    let p: AuthPrincipal = serde_json::from_slice(&bytes).map_err(|_| invalid())?;
    if p.user_id > i64::MAX as u64
        || p.roles.iter().any(|r| r.trim().is_empty())
        || (p.user_id == 0 && (!p.roles.is_empty() || !p.data_permissions.is_empty()))
        || p.data_permissions.iter().any(|d| {
            ![
                "BLOG_VIEW_ALL",
                "BLOG_EDIT_ALL",
                "BLOG_DELETE_ALL",
                "BLOG_EXPORT_ALL",
            ]
            .contains(&d.as_str())
        })
    {
        return Err(invalid());
    }
    Ok(p)
}
pub struct WebError(pub u16, pub i32, pub String);
impl WebError {
    pub fn validation(msg: &str) -> Self {
        Self(400, 1000, msg.into())
    }
    pub fn token() -> Self {
        Self(401, 8, "token非法".into())
    }
    pub fn denied() -> Self {
        Self(403, 51, "没有权限".into())
    }
}
impl From<anyhow::Error> for WebError {
    fn from(e: anyhow::Error) -> Self {
        if let Some(r) = e.downcast_ref::<RpcError>() {
            return Self(r.status, r.code, r.message.clone());
        }
        tracing::error!(error=%e,"request failed");
        Self(500, 9999, "internal server error".into())
    }
}
impl From<RpcError> for WebError {
    fn from(e: RpcError) -> Self {
        Self(e.status, e.code, e.message)
    }
}
impl IntoResponse for WebError {
    fn into_response(self) -> Response {
        (
            StatusCode::from_u16(self.0).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR),
            Json(RpcResult::<()>::fail(self.1, self.2)),
        )
            .into_response()
    }
}
pub fn ok<T: Serialize>(value: Option<T>) -> Response {
    Json(RpcResult::success(value)).into_response()
}
pub async fn not_found(uri: axum::http::Uri) -> Response {
    let now =
        chrono::Utc::now().with_timezone(&chrono::FixedOffset::east_opt(8 * 3600).expect("GMT+8"));
    (
        StatusCode::NOT_FOUND,
        Json(serde_json::json!({
            "timestamp": now.format("%Y-%m-%d %H:%M:%S").to_string(),
            "status": 404, "error":"Not Found", "path":uri.path()
        })),
    )
        .into_response()
}

pub async fn serve(
    port: u16,
    router: Router,
    cache: std::sync::Arc<cache_rs::Cache>,
) -> Result<()> {
    let readiness = move || {
        let cache = cache.clone();
        async move {
            let ready = tokio::time::timeout(std::time::Duration::from_secs(3), cache.ready())
                .await
                .unwrap_or(false);
            (
                if ready {
                    StatusCode::OK
                } else {
                    StatusCode::SERVICE_UNAVAILABLE
                },
                Json(serde_json::json!({"status": if ready { "UP" } else { "DOWN" }})),
            )
        }
    };
    let app = router
        .route("/actuator/health", get(readiness.clone()))
        .route(
            "/actuator/health/liveness",
            get(|| async { Json(serde_json::json!({"status":"UP"})) }),
        )
        .route("/actuator/health/readiness", get(readiness))
        .fallback(not_found)
        .method_not_allowed_fallback(not_found)
        .layer(from_fn(|req: Request, next: Next| async move {
            let route = req.extensions().get::<axum::extract::MatchedPath>()
                .map(|p| p.as_str()).unwrap_or("unknown");
            let span = tracing::info_span!("HTTP request",http.request.method=%req.method(),http.route=route);
            megalith_otel::set_parent_from_headers(&span, req.headers());
            next.run(req).instrument(span).await
        }));
    axum::serve(tokio::net::TcpListener::bind(("0.0.0.0", port)).await?, app)
        .with_graceful_shutdown(shutdown_signal())
        .await?;
    Ok(())
}
async fn shutdown_signal() {
    #[cfg(unix)]
    {
        use tokio::signal::unix::{SignalKind, signal};
        let mut terminate = signal(SignalKind::terminate()).expect("install SIGTERM handler");
        tokio::select! { _ = terminate.recv() => {}, _ = tokio::signal::ctrl_c() => {} }
    }
    #[cfg(not(unix))]
    {
        let _ = tokio::signal::ctrl_c().await;
    }
}

pub fn run<F: Future<Output = Result<()>>>(name: &'static str, port: u16, app: F) -> Result<()> {
    let mut args = std::env::args_os();
    let probe = args.next().is_some_and(|x| {
        std::path::Path::new(&x)
            .file_name()
            .is_some_and(|n| n == "thc")
    });
    if probe || args.next().as_deref() == Some(std::ffi::OsStr::new("health")) {
        let res = reqwest::blocking::Client::builder()
            .timeout(std::time::Duration::from_secs(4))
            .build()?
            .get(format!("http://127.0.0.1:{port}/actuator/health"))
            .send()?;
        ensure!(res.status().is_success(), "unhealthy");
        return Ok(());
    }
    let defaults = std::env::var("RUST_LOG").unwrap_or_else(|_| "info".into());
    let ratio = std::env::var("management.tracing.sampling.probability")
        .or_else(|_| std::env::var("MANAGEMENT_TRACING_SAMPLING_PROBABILITY"))
        .ok()
        .and_then(|v| v.parse().ok())
        .unwrap_or(0.05);
    let providers =
        megalith_otel::otlp_config().init(name, env!("CARGO_PKG_VERSION"), &defaults, ratio)?;
    let result = tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()?
        .block_on(app);
    providers.shutdown();
    result
}
