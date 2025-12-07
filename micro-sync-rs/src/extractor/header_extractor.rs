use opentelemetry::propagation::Extractor;
use warp::http::HeaderMap;

/// 自定义 HeaderExtractor，用于从 warp 的 HeaderMap (http 0.2) 中提取 trace context
pub struct WarpHeaderExtractor<'a>(pub &'a HeaderMap);

impl Extractor for WarpHeaderExtractor<'_> {
    fn get(&self, key: &str) -> Option<&str> {
        self.0.get(key).and_then(|v| v.to_str().ok())
    }

    fn keys(&self) -> Vec<&str> {
        self.0.keys().map(|k| k.as_str()).collect()
    }
}
