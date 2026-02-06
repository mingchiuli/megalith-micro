use axum::http::HeaderMap;
use opentelemetry::propagation::Extractor;

/// 自定义 HeaderExtractor，用于从 axum 的 HeaderMap (http 1.x) 中提取 trace context
pub struct AxumHeaderExtractor<'a>(pub &'a HeaderMap);

impl Extractor for AxumHeaderExtractor<'_> {
    fn get(&self, key: &str) -> Option<&str> {
        self.0.get(key).and_then(|v| v.to_str().ok())
    }

    fn keys(&self) -> Vec<&str> {
        self.0.keys().map(|k| k.as_str()).collect()
    }
}
