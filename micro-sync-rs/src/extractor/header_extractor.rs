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

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn get_returns_value_for_existing_key() {
        let mut h = HeaderMap::new();
        h.insert("traceparent", "abc".parse().unwrap());
        let ex = AxumHeaderExtractor(&h);
        assert_eq!(ex.get("traceparent"), Some("abc"));
    }

    #[test]
    fn get_returns_none_for_missing_key() {
        let h = HeaderMap::new();
        let ex = AxumHeaderExtractor(&h);
        assert!(ex.get("missing").is_none());
    }

    #[test]
    fn keys_lists_all_headers() {
        let mut h = HeaderMap::new();
        h.insert("a", "1".parse().unwrap());
        h.insert("b", "2".parse().unwrap());
        let ex = AxumHeaderExtractor(&h);
        let keys = ex.keys();
        assert!(keys.contains(&"a"));
        assert!(keys.contains(&"b"));
        assert_eq!(keys.len(), 2);
    }
}
