use axum::http::{Method, header};
use axum::{
    extract::{Request, State},
    middleware::Next,
    response::Response,
};
use url::Url;

use crate::GatewayState;
use crate::config::{self, ConfigKey};
use crate::exception::HandlerError;
use crate::utils;

use tracing::Instrument;

pub async fn process(
    State(state): State<GatewayState>,
    mut req: Request,
    next: Next,
) -> Result<Response, HandlerError> {
    // Skip authentication for actuator endpoints
    if req.uri().path() == "/actuator/health" {
        return Ok(next.run(req).await);
    }

    validate_cookie_origin(&req)?;

    let method = req.method();
    let path = req.uri().path();

    // 手动创建动态名称的 span
    let span = tracing::info_span!(
        "request_handler",  // 这个是 tracing 内部名称
        http.method = %method,
        http.uri = %path,
        otel.name = format!("Request: {} {}", method, path)  // 这个才是显示的名称
    );

    async move {
        let token = if req.uri().path() == "/token/refresh" {
            String::new()
        } else {
            utils::extract_token(&req)
        };
        let auth_url = utils::get_auth_url()?;
        let route_request = utils::prepare_route_request(req.method(), req.headers(), req.uri());
        let route = utils::find_route(&state, auth_url, route_request, &token).await?;
        req.extensions_mut().insert(route);
        Ok(next.run(req).await)
    }
    .instrument(span)
    .await
}

fn validate_cookie_origin(req: &Request) -> Result<(), HandlerError> {
    let origin = request_origin(req);

    if origin.is_none() && !requires_trusted_source(req) && !has_source_header(req) {
        return Ok(());
    }

    let allowed = config::get_config(ConfigKey::AllowedOrigins);
    if origin.is_some_and(|value| is_allowed_origin(&value, &allowed)) {
        return Ok(());
    }

    Err(HandlerError::forbidden("请求来源不受信任"))
}

fn requires_trusted_source(req: &Request) -> bool {
    !matches!(
        req.method(),
        &Method::GET | &Method::HEAD | &Method::OPTIONS
    ) || utils::is_websocket_request(req)
        || utils::has_auth_cookie(req.headers())
}

fn has_source_header(req: &Request) -> bool {
    req.headers().contains_key(header::ORIGIN) || req.headers().contains_key(header::REFERER)
}

fn request_origin(req: &Request) -> Option<String> {
    if let Some(origin) = req.headers().get(header::ORIGIN) {
        return origin.to_str().ok().map(String::from);
    }

    req.headers()
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
    use super::{is_allowed_origin, request_origin, requires_trusted_source};
    use axum::{body::Body, extract::Request};

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
            let req = Request::builder()
                .method(method)
                .uri("/x")
                .body(Body::empty())
                .unwrap();
            assert!(requires_trusted_source(&req));
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
