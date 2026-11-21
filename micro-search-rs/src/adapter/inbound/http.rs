use crate::{
    application::{
        Search,
        contract::{
            AdminQuery, FeedQuery, ReadCount, RecommendationFeed, RevisionQuery, SearchPage,
        },
        feed,
        rebuild::Conflict,
    },
    domain,
};
use axum::{
    Json, Router,
    extract::{
        MatchedPath, Query, Request, State,
        rejection::{JsonRejection, QueryRejection},
    },
    http::StatusCode,
    middleware::{self, Next},
    response::{IntoResponse, Response},
    routing::{get, post},
};
use opentelemetry::{KeyValue, global};
use serde::{Deserialize, Serialize};
use serde_json::{Value, json};
use tracing::Instrument;

pub fn router(app: Search) -> Router {
    Router::new()
        .route("/actuator/health", get(health))
        .route("/actuator/health/readiness", get(health))
        .route(
            "/actuator/health/liveness",
            get(|| async { Json(json!({"status":"UP"})) }),
        )
        .route("/search/public/blog", get(public))
        .route("/inner/blog/admin/search", post(admin))
        .route("/inner/blog/revisions/search", post(revisions))
        .route(
            "/inner/blog/public/recommendations/feed",
            get(recommendations),
        )
        .route("/inner/blog/views/batch", post(counts))
        .route("/inner/search/index/rebuild", post(rebuild))
        .layer(middleware::from_fn(trace))
        .with_state(app)
}
async fn trace(req: Request, next: Next) -> Response {
    let route = req
        .extensions()
        .get::<MatchedPath>()
        .map(|x| x.as_str())
        .unwrap_or("unknown")
        .to_owned();
    let span = tracing::info_span!(
        "search.request", http.route = route, http.request.method = %req.method(), otel.kind = "server"
    );
    megalith_otel::set_parent_from_headers(&span, req.headers());
    let start = std::time::Instant::now();
    let response = next.run(req).instrument(span).await;
    static DURATION: std::sync::LazyLock<opentelemetry::metrics::Histogram<f64>> =
        std::sync::LazyLock::new(|| {
            global::meter("micro-search")
                .f64_histogram("http.server.request.duration")
                .with_unit("s")
                .build()
        });
    DURATION.record(
        start.elapsed().as_secs_f64(),
        &[
            KeyValue::new("http.route", route),
            KeyValue::new(
                "http.response.status_code",
                response.status().as_u16() as i64,
            ),
        ],
    );
    response
}
async fn health(State(s): State<Search>) -> impl IntoResponse {
    let ok =
        s.store.ready().await && s.broker.ready().await && s.engine.get("/health").await.is_ok();
    (
        if ok {
            StatusCode::OK
        } else {
            StatusCode::SERVICE_UNAVAILABLE
        },
        Json(json!({"status":if ok{"UP"}else{"DOWN"}})),
    )
}
struct Error(StatusCode, i32, String);
impl IntoResponse for Error {
    fn into_response(self) -> Response {
        (self.0, Json(json!({"code":self.1,"msg":self.2}))).into_response()
    }
}
impl From<anyhow::Error> for Error {
    fn from(e: anyhow::Error) -> Self {
        if let Some(c) = e.downcast_ref::<Conflict>() {
            Self(StatusCode::CONFLICT, 2000, c.to_string())
        } else {
            tracing::error!(error=%e,"search operation failed");
            Self(
                StatusCode::INTERNAL_SERVER_ERROR,
                9999,
                "internal server error".into(),
            )
        }
    }
}
impl From<QueryRejection> for Error {
    fn from(e: QueryRejection) -> Self {
        invalid(&e.body_text())
    }
}
impl From<JsonRejection> for Error {
    fn from(_: JsonRejection) -> Self {
        Self(
            StatusCode::BAD_REQUEST,
            1001,
            "request body is malformed".into(),
        )
    }
}
type Reply<T = Value> = Result<Json<Success<T>>, Error>;

#[derive(Serialize)]
struct Success<T> {
    code: i32,
    msg: &'static str,
    #[serde(skip_serializing_if = "Option::is_none")]
    data: Option<T>,
}

fn invalid(msg: &str) -> Error {
    Error(StatusCode::BAD_REQUEST, 1000, msg.into())
}
fn ok<T>(data: Option<T>) -> Json<Success<T>> {
    Json(Success {
        code: 200,
        msg: "success",
        data,
    })
}
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct PublicQuery {
    current_page: u32,
    keywords: String,
    all_info: String,
}
async fn public(State(s): State<Search>, q: Result<Query<PublicQuery>, QueryRejection>) -> Reply {
    let Query(q) = q?;
    if q.current_page == 0 || !(1..=20).contains(&q.keywords.encode_utf16().count()) {
        return Err(invalid("invalid currentPage or keywords"));
    }
    let all = q
        .all_info
        .to_ascii_lowercase()
        .parse()
        .map_err(|_| invalid("allInfo must be a boolean"))?;
    Ok(ok(Some(
        s.public_search(q.current_page, q.keywords, all).await?,
    )))
}
async fn admin(
    State(s): State<Search>,
    q: Result<Json<AdminQuery>, JsonRejection>,
) -> Reply<SearchPage> {
    let Json(q) = q?;
    if q.page == 0
        || q.page_size == 0
        || q.user_id < 0
        || q.status.is_some_and(|n| !(0..=3).contains(&n))
        || q.keywords
            .as_ref()
            .is_some_and(|v| v.encode_utf16().count() > 20)
    {
        return Err(invalid("invalid admin search parameters"));
    }
    let start = q
        .create_start
        .as_deref()
        .map(domain::millis)
        .transpose()
        .map_err(|_| invalid("invalid createStart"))?;
    let end = q
        .create_end
        .as_deref()
        .map(domain::millis)
        .transpose()
        .map_err(|_| invalid("invalid createEnd"))?;
    if matches!((start,end),(Some(a),Some(b)) if a>b) {
        return Err(invalid("invalid date range"));
    }
    Ok(ok(Some(s.admin_search(q).await?)))
}
async fn revisions(
    State(s): State<Search>,
    q: Result<Json<RevisionQuery>, JsonRejection>,
) -> Reply<SearchPage> {
    let Json(q) = q?;
    if q.blog_id < 1 || q.user_id < 1 || q.page == 0 || !(1..=100).contains(&q.page_size) {
        return Err(invalid("invalid revision search parameters"));
    }
    Ok(ok(Some(s.revisions(q).await?)))
}
async fn recommendations(
    State(s): State<Search>,
    q: Result<Query<FeedQuery>, QueryRejection>,
) -> Reply<RecommendationFeed> {
    let Query(q) = q?;
    if !(1..=50).contains(&q.size) {
        return Err(invalid("invalid size"));
    }
    let raw = q.cursor.filter(|v| !v.trim().is_empty());
    let cursor = raw
        .as_deref()
        .map(feed::decode)
        .transpose()
        .map_err(|_| invalid("invalid recommendation cursor"))?
        .flatten();
    Ok(ok(Some(s.feed(cursor, q.size, raw.is_some()).await?)))
}
async fn counts(
    State(s): State<Search>,
    rows: Result<Json<Vec<ReadCount>>, JsonRejection>,
) -> Reply<()> {
    let Json(rows) = rows?;
    if !(1..=1000).contains(&rows.len()) || rows.iter().any(|v| v.blog_id < 1 || v.read_count < 0) {
        return Err(invalid("invalid read counts"));
    }
    s.update_counts(rows).await?;
    Ok(ok(None))
}
async fn rebuild(State(s): State<Search>) -> Reply {
    let meter = global::meter("micro-search");
    let start = std::time::Instant::now();
    let result = s.rebuild().await;
    meter
        .f64_histogram("megalith.search.rebuild.duration")
        .with_unit("s")
        .build()
        .record(start.elapsed().as_secs_f64(), &[]);
    match &result {
        Ok(result) => {
            let count = result["indexes"]
                .as_array()
                .into_iter()
                .flatten()
                .filter_map(|x| x["documents"].as_u64())
                .sum();
            meter
                .u64_counter("megalith.search.rebuild.documents")
                .build()
                .add(count, &[]);
            meter
                .i64_gauge("megalith.search.rebuild.last.success")
                .build()
                .record(jiff::Timestamp::now().as_second(), &[]);
        }
        Err(_) => meter
            .u64_counter("megalith.search.rebuild.failures")
            .build()
            .add(1, &[]),
    }
    Ok(ok(Some(result?)))
}
