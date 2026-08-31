use axum::extract::Request;
use axum::http::{Method, header};
use url::Url;

use super::request::{has_auth_cookie, is_websocket_request};
use crate::config::{self, ConfigKey};
use crate::exception::HandlerError;

pub(crate) fn validate_cookie_origin(request: &Request) -> Result<(), HandlerError> {
    let origin = request_origin(request);
    if origin.is_none() && !requires_trusted_source(request) && !has_source_header(request) {
        return Ok(());
    }

    let allowed = config::get_config(ConfigKey::AllowedOrigins);
    if origin.is_some_and(|value| is_allowed_origin(&value, &allowed)) {
        return Ok(());
    }

    Err(HandlerError::forbidden("请求来源不受信任"))
}

fn requires_trusted_source(request: &Request) -> bool {
    !matches!(
        request.method(),
        &Method::GET | &Method::HEAD | &Method::OPTIONS
    ) || is_websocket_request(request)
        || has_auth_cookie(request.headers())
}

fn has_source_header(request: &Request) -> bool {
    request.headers().contains_key(header::ORIGIN)
        || request.headers().contains_key(header::REFERER)
}

fn request_origin(request: &Request) -> Option<String> {
    if let Some(origin) = request.headers().get(header::ORIGIN) {
        return origin.to_str().ok().map(String::from);
    }

    request
        .headers()
        .get(header::REFERER)
        .and_then(|value| value.to_str().ok())
        .and_then(|value| Url::parse(value).ok())
        .map(|url| url.origin().ascii_serialization())
}

fn is_allowed_origin(origin: &str, allowed_origins: &str) -> bool {
    !origin.is_empty()
        && allowed_origins
            .split(',')
            .map(str::trim)
            .any(|value| value == origin)
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::body::Body;

    #[test]
    fn matches_a_trimmed_origin_from_the_allowlist() {
        assert!(is_allowed_origin(
            "https://chiu.wiki",
            "http://localhost:1919, https://chiu.wiki"
        ));
    }

    #[test]
    fn rejects_empty_and_partial_origins() {
        assert!(!is_allowed_origin("", "https://chiu.wiki"));
        assert!(!is_allowed_origin(
            "https://chiu.wiki.evil",
            "https://chiu.wiki"
        ));
    }

    #[test]
    fn requires_trusted_source_for_unsafe_methods() {
        for method in ["POST", "PUT", "PATCH", "DELETE"] {
            let request = Request::builder()
                .method(method)
                .uri("/x")
                .body(Body::empty())
                .unwrap();
            assert!(requires_trusted_source(&request));
        }
    }

    #[test]
    fn authenticated_safe_reads_require_a_trusted_source() {
        let authenticated = Request::builder()
            .uri("/x")
            .header("Cookie", "megalith_access_token=access-jwt")
            .body(Body::empty())
            .unwrap();
        let public = Request::builder().uri("/x").body(Body::empty()).unwrap();

        assert!(requires_trusted_source(&authenticated));
        assert!(!requires_trusted_source(&public));
    }

    #[test]
    fn derives_an_exact_origin_from_referer() {
        let request = Request::builder()
            .uri("/x")
            .header("Referer", "https://chiu.wiki/blog?id=1")
            .body(Body::empty())
            .unwrap();

        assert_eq!(
            request_origin(&request).as_deref(),
            Some("https://chiu.wiki")
        );
    }

    #[test]
    fn websocket_gets_require_a_trusted_source() {
        let websocket = Request::builder()
            .uri("/rooms")
            .header("upgrade", "websocket")
            .body(Body::empty())
            .unwrap();
        assert!(requires_trusted_source(&websocket));
    }
}
