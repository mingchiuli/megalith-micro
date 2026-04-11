use axum::{
    extract::Request,
    middleware::Next,
    response::Response,
};
use opentelemetry::global;
use tracing_opentelemetry::OpenTelemetrySpanExt;

use crate::extractor::AxumHeaderExtractor;

/// Trace Context Extract 中间件
///
/// 自动从请求头提取上游 Trace Context 并关联到当前 Span
pub async fn trace_context_middleware(req: Request, next: Next) -> Response {
    // 1. 提取上游 Trace Context
    let parent_context = global::get_text_map_propagator(|propagator| {
        propagator.extract(&AxumHeaderExtractor(req.headers()))
    });

    // 2. 获取当前 span 并设置父上下文
    let span = tracing::Span::current();
    let _ = span.set_parent(parent_context);

    // 3. 继续处理请求
    next.run(req).await
}
