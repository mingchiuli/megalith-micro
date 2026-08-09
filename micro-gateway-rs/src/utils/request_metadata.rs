use axum::extract::Request;
use hyper::{HeaderMap, Uri, header};

use crate::constant::{
    self, AUTH_HEADER, CF_CONNECTING_IP, FORWARDED_HEADER, PROXY_CLIENT_IP, WL_PROXY_CLIENT_IP,
};

pub(crate) const ACCESS_TOKEN_COOKIE: &str = "megalith_access_token";
pub(crate) const REFRESH_TOKEN_COOKIE: &str = "megalith_refresh_token";

pub fn get_ip_from_headers(headers: &HeaderMap) -> Option<String> {
    headers
        .get(CF_CONNECTING_IP)
        .or_else(|| headers.get(AUTH_HEADER))
        .or_else(|| headers.get(FORWARDED_HEADER))
        .or_else(|| headers.get(PROXY_CLIENT_IP))
        .or_else(|| headers.get(WL_PROXY_CLIENT_IP))
        .and_then(|value| value.to_str().ok())
        .and_then(|value| value.split(',').next())
        .map(String::from)
}

pub fn token_from_query(uri: &Uri) -> Option<String> {
    uri.query().and_then(|query| {
        url::form_urlencoded::parse(query.as_bytes())
            .find(|(key, _)| key == constant::TOKEN)
            .map(|(_, value)| value.to_string())
    })
}

pub fn extract_token(req: &Request) -> String {
    if is_websocket_request(req) {
        token_from_query(req.uri()).unwrap_or_default()
    } else {
        cookie_value(req.headers(), ACCESS_TOKEN_COOKIE)
            .map(|value| format!("Bearer {value}"))
            .unwrap_or_default()
    }
}

pub fn is_websocket_request(req: &Request) -> bool {
    req.headers().get("sec-websocket-version").is_some()
        || req
            .headers()
            .get(header::UPGRADE)
            .and_then(|value| value.to_str().ok())
            .is_some_and(|value| value.eq_ignore_ascii_case("websocket"))
}

pub(crate) fn cookie_value(headers: &HeaderMap, name: &str) -> Option<String> {
    headers
        .get(header::COOKIE)
        .and_then(|value| value.to_str().ok())
        .and_then(|cookies| {
            cookies.split(';').find_map(|cookie| {
                let (cookie_name, value) = cookie.trim().split_once('=')?;
                (cookie_name == name).then(|| value.to_string())
            })
        })
}

pub(crate) fn has_auth_cookie(headers: &HeaderMap) -> bool {
    cookie_value(headers, ACCESS_TOKEN_COOKIE).is_some()
        || cookie_value(headers, REFRESH_TOKEN_COOKIE).is_some()
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::body::Body;

    #[test]
    fn client_ip_uses_header_precedence_and_first_forwarded_value() {
        let mut headers = HeaderMap::new();
        headers.insert("X-Forwarded-For", "2.2.2.2, 3.3.3.3".parse().unwrap());
        headers.insert("X-Real-IP", "1.1.1.1".parse().unwrap());
        assert_eq!(get_ip_from_headers(&headers).as_deref(), Some("1.1.1.1"));

        headers.insert("CF-Connecting-IP", "4.4.4.4".parse().unwrap());
        assert_eq!(get_ip_from_headers(&headers).as_deref(), Some("4.4.4.4"));
    }

    #[test]
    fn client_ip_is_absent_without_forwarding_headers() {
        assert!(get_ip_from_headers(&HeaderMap::new()).is_none());
    }

    #[test]
    fn access_cookie_becomes_bearer_token() {
        let request = Request::builder()
            .uri("/x")
            .header("Cookie", "other=1; megalith_access_token=access-jwt")
            .body(Body::empty())
            .unwrap();
        assert_eq!(extract_token(&request), "Bearer access-jwt");
    }

    #[test]
    fn detects_access_and_refresh_cookies() {
        let access = Request::builder()
            .uri("/x")
            .header("Cookie", "megalith_access_token=access-jwt")
            .body(Body::empty())
            .unwrap();
        let refresh = Request::builder()
            .uri("/x")
            .header("Cookie", "megalith_refresh_token=refresh-jwt")
            .body(Body::empty())
            .unwrap();
        let unrelated = Request::builder()
            .uri("/x")
            .header("Cookie", "megalith_theme=dark")
            .body(Body::empty())
            .unwrap();

        assert!(has_auth_cookie(access.headers()));
        assert!(has_auth_cookie(refresh.headers()));
        assert!(!has_auth_cookie(unrelated.headers()));
    }

    #[test]
    fn websocket_uses_query_token() {
        let request = Request::builder()
            .uri("/ws?token=ws-token-1")
            .header("upgrade", "websocket")
            .body(Body::empty())
            .unwrap();
        assert_eq!(extract_token(&request), "ws-token-1");
    }

    #[test]
    fn missing_token_is_empty_for_http_and_websocket() {
        let http = Request::builder().uri("/x").body(Body::empty()).unwrap();
        let websocket = Request::builder()
            .uri("/ws")
            .header("sec-websocket-version", "13")
            .body(Body::empty())
            .unwrap();
        assert_eq!(extract_token(&http), "");
        assert_eq!(extract_token(&websocket), "");
    }
}
