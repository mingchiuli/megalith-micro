use axum::{extract::Request, middleware::Next, response::Response};

pub async fn trace_context_middleware(req: Request, next: Next) -> Response {
    megalith_otel::set_parent_from_headers(&tracing::Span::current(), req.headers());
    next.run(req).await
}
