use axum::{extract::Request, middleware::Next, response::Response};
use megalith_otel::set_parent_from_headers;
use tracing::Instrument;

pub async fn trace_context_middleware(req: Request, next: Next) -> Response {
    let span = tracing::info_span!("gateway_request");
    set_parent_from_headers(&span, req.headers());
    next.run(req).instrument(span).await
}
