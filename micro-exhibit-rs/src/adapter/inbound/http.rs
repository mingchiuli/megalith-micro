use crate::application::Exhibit;
use axum::{
    Router,
    body::Bytes,
    extract::{Path, Query, State},
    http::HeaderMap,
    response::Response,
    routing::{get, post},
};
use megalith_service_runtime::web::{WebError, ok, principal};
use serde::Deserialize;

pub fn router(app: Exhibit) -> Router {
    Router::new()
        .route("/public/blog/info/{id}", get(detail))
        .route("/public/blog/secret/{id}", post(secret))
        .route("/public/blog/stat", get(visits))
        .route("/public/blog/recommendations", get(feed))
        .with_state(app)
}
fn id(raw: String) -> Result<i64, WebError> {
    raw.parse::<i64>()
        .ok()
        .filter(|id| *id > 0)
        .ok_or_else(|| WebError::validation("blogId must be positive"))
}
async fn detail(
    State(app): State<Exhibit>,
    Path(raw): Path<String>,
    headers: HeaderMap,
) -> Result<Response, WebError> {
    Ok(ok(Some(
        app.get_blog_detail(id(raw)?, principal(&headers)?).await?,
    )))
}
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct ReadToken {
    read_token: Option<String>,
}
async fn secret(
    State(app): State<Exhibit>,
    Path(raw): Path<String>,
    body: Bytes,
) -> Result<Response, WebError> {
    let id = id(raw)?;
    let req: ReadToken = serde_json::from_slice(&body)
        .map_err(|_| WebError(400, 1001, "request body is malformed".into()))?;
    let token = req
        .read_token
        .filter(|t| !t.trim().is_empty())
        .ok_or_else(|| WebError::validation("readToken must not be blank"))?;
    Ok(ok(Some(app.get_locked_blog(id, &token).await?)))
}
async fn visits(State(app): State<Exhibit>) -> Result<Response, WebError> {
    let [day, week, month, year] = app.store.visits().await?;
    Ok(ok(Some(
        serde_json::json!({"dayVisit":day,"weekVisit":week,"monthVisit":month,"yearVisit":year}),
    )))
}
#[derive(Deserialize)]
struct FeedQuery {
    cursor: Option<String>,
}
async fn feed(
    State(app): State<Exhibit>,
    Query(q): Query<FeedQuery>,
) -> Result<Response, WebError> {
    if q.cursor
        .as_deref()
        .is_some_and(|s| s.encode_utf16().count() > 4096)
    {
        return Err(WebError::validation("cursor is too long"));
    }
    Ok(ok(Some(app.recommendations.feed(q.cursor).await?)))
}
