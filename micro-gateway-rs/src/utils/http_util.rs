use axum::{
    BoxError,
    body::{Body, Bytes},
    extract::Request,
    http::{HeaderName, HeaderValue, request::Builder},
    response::Response,
};

use http_body_util::combinators::BoxBody;

use hyper::{HeaderMap, Method, StatusCode, Uri, header};
use std::collections::HashMap;

use crate::{
    client::{self, AuthRouteReq, AuthRouteResp},
    config::config::{self, ConfigKey},
    constant::{
        AUTH_HEADER, CF_CONNECTING_IP, FORWARDED_HEADER, PROXY_CLIENT_IP, UNKNOWN,
        WL_PROXY_CLIENT_IP,
    },
    exception::{AuthError, ClientError},
    result::ApiResult,
};

use opentelemetry::global;
use opentelemetry_http::HeaderInjector;
use tracing_opentelemetry::OpenTelemetrySpanExt;

pub fn get_ip_from_headers(headers: &HeaderMap) -> Option<String> {
    // Check headers in order of preference
    let ip = headers
        .get(CF_CONNECTING_IP) // Cloudflare
        .or_else(|| headers.get(AUTH_HEADER)) // Nginx
        .or_else(|| headers.get(FORWARDED_HEADER)) // General proxy
        .or_else(|| headers.get(PROXY_CLIENT_IP))
        .or_else(|| headers.get(WL_PROXY_CLIENT_IP))
        .and_then(|hv| hv.to_str().ok())
        .and_then(|s| s.split(',').next());

    ip.map(String::from)
}

pub fn extract_token(req: &Request) -> String {
    if req.headers().get("sec-websocket-version").is_some()
        || req
            .headers()
            .get("upgrade")
            .map(|h| h.to_str().unwrap_or("").to_lowercase() == "websocket")
            .unwrap_or(false)
    {
        // WebSocket 协议 - 从查询参数获取 token
        req.uri()
            .query()
            .and_then(|q| {
                url::form_urlencoded::parse(q.as_bytes())
                    .find(|(key, _)| key == "token")
                    .map(|(_, value)| value.to_string())
            })
            .unwrap_or_default()
    } else {
        // 其他协议 - 从 Authorization 头获取
        req.headers()
            .get("Authorization")
            .and_then(|h| h.to_str().ok())
            .unwrap_or("")
            .to_string()
    }
}

pub fn get_auth_url() -> Result<hyper::Uri, AuthError> {
    let mut auth_url = config::get_config(ConfigKey::AuthUrlKey);

    auth_url.push_str("/auth/route");
    Ok(auth_url
        .parse::<hyper::Uri>()
        .map_err(|e| AuthError::InvalidUrl(e.to_string()))?)
}

pub fn set_headers(mut builder: Builder, headers: HashMap<HeaderName, HeaderValue>) -> Builder {
    for (key, value) in headers {
        builder = builder.header(key, value);
    }
    builder
}

#[tracing::instrument(
    name = "find_target_address",
    skip(req_body, token),
    fields(
        http.url = %auth_url
    )
)]
pub async fn find_route(
    auth_url: Uri,
    req_body: AuthRouteReq,
    token: &str,
) -> Result<AuthRouteResp, ClientError> {
    let headers = HashMap::from([
        (
            hyper::header::AUTHORIZATION,
            HeaderValue::from_str(token).unwrap_or(HeaderValue::from_static("")),
        ),
        (
            hyper::header::CONTENT_TYPE,
            HeaderValue::from_static("application/json"),
        ),
    ]);

    let resp: ApiResult<AuthRouteResp> = client::post(auth_url, req_body, headers)
        .await
        .map_err(|e| ClientError::Status(StatusCode::BAD_GATEWAY.as_u16(), e.to_string()))?;
    Ok(resp.into_data())
}

pub fn prepare_route_request(method: &Method, headers: &HeaderMap, uri: &Uri) -> AuthRouteReq {
    let ip_addr = get_ip_from_headers(headers).unwrap_or_else(|| UNKNOWN.to_string());

    AuthRouteReq::new(method, uri.path().to_string(), ip_addr)
}

pub fn parse_url(route_resp: AuthRouteResp, uri: &Uri, protocol: &str) -> Result<Uri, ClientError> {
    let path_and_query = uri
        .path_and_query()
        .ok_or_else(|| ClientError::Request("Invalid URI".to_string()))?
        .to_string();

    let uri = format!(
        "{}://{}:{}{}",
        protocol,
        route_resp.service_host(),
        route_resp.service_port(),
        path_and_query
    );

    Ok(uri
        .parse::<hyper::Uri>()
        .map_err(|e| ClientError::Request(format!("parse: {}", e.to_string())))?)
}

pub fn prepare_headers(
    req_headers: &hyper::HeaderMap,
    token: String,
) -> std::result::Result<HashMap<HeaderName, HeaderValue>, ClientError> {
    let mut headers = HashMap::new();

    headers.insert(
        header::AUTHORIZATION,
        HeaderValue::from_str(&token).unwrap_or(HeaderValue::from_static("")),
    );

    let content_type = req_headers
        .get(header::CONTENT_TYPE)
        .unwrap_or(&HeaderValue::from_static("application/json"))
        .to_str()
        .map_err(|e| ClientError::Request(e.to_string()))?
        .to_string();

    headers.insert(
        header::CONTENT_TYPE,
        HeaderValue::from_str(&content_type)
            .unwrap_or(HeaderValue::from_static("application/json")),
    );

    Ok(headers)
}

pub fn prepare_response(
    resp: Response<BoxBody<Bytes, BoxError>>, // ← 修改参数类型
) -> Result<Response<Body>, ClientError> {
    let content_type = resp
        .headers()
        .get(hyper::header::CONTENT_TYPE)
        .unwrap_or(&HeaderValue::from_static("application/json"))
        .to_str()
        .map_err(|e| ClientError::Response(e.to_string()))?
        .to_string();

    let status = resp.status();
    let (parts, body) = resp.into_parts();

    // 将 BoxBody 转换为 axum::body::Body
    let axum_body = Body::new(body);

    let mut builder = Response::builder().status(status);

    // 保留原有的 headers
    for (key, value) in parts.headers.iter() {
        builder = builder.header(key, value);
    }

    // 设置或覆盖 Content-Type
    if content_type == "application/octet-stream" {
        builder = builder.header(header::CONTENT_TYPE, "application/octet-stream");
    } else if !parts.headers.contains_key(header::CONTENT_TYPE) {
        builder = builder.header(header::CONTENT_TYPE, "application/json");
    }

    Ok(builder
        .body(axum_body)
        .map_err(|e| ClientError::Response(e.to_string()))?)
}

// 原有 hyper::HeaderMap 版本（WebSocket 用）
pub fn inject_trace_context(headers: &mut HeaderMap) {
    global::get_text_map_propagator(|propagator| {
        let current_context = tracing::Span::current().context();
        propagator.inject_context(&current_context, &mut HeaderInjector(headers));
    });
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::body::Body;

    #[test]
    fn ip_from_cf_header() {
        let mut h = HeaderMap::new();
        h.insert("CF-Connecting-IP", "1.2.3.4".parse().unwrap());
        assert_eq!(get_ip_from_headers(&h), Some("1.2.3.4".to_string()));
    }

    #[test]
    fn ip_takes_first_in_forwarded_chain() {
        let mut h = HeaderMap::new();
        h.insert("X-Forwarded-For", "1.1.1.1, 2.2.2.2".parse().unwrap());
        assert_eq!(get_ip_from_headers(&h), Some("1.1.1.1".to_string()));
    }

    #[test]
    fn ip_returns_none_when_absent() {
        let h = HeaderMap::new();
        assert!(get_ip_from_headers(&h).is_none());
    }

    #[test]
    fn ip_prefers_cf_over_others() {
        let mut h = HeaderMap::new();
        h.insert("X-Real-IP", "9.9.9.9".parse().unwrap());
        h.insert("CF-Connecting-IP", "1.1.1.1".parse().unwrap());
        assert_eq!(get_ip_from_headers(&h), Some("1.1.1.1".to_string()));
    }

    #[test]
    fn extract_token_from_authorization() {
        let req = Request::builder()
            .uri("/x")
            .header("Authorization", "Bearer abc")
            .body(Body::empty())
            .unwrap();
        assert_eq!(extract_token(&req), "Bearer abc");
    }

    #[test]
    fn extract_token_returns_empty_when_absent() {
        let req = Request::builder().uri("/x").body(Body::empty()).unwrap();
        assert_eq!(extract_token(&req), "");
    }

    #[test]
    fn extract_token_from_websocket_query() {
        let req = Request::builder()
            .uri("/ws?token=ws-token-1")
            .header("upgrade", "websocket")
            .body(Body::empty())
            .unwrap();
        assert_eq!(extract_token(&req), "ws-token-1");
    }

    #[test]
    fn extract_token_websocket_without_token_query() {
        let req = Request::builder()
            .uri("/ws")
            .header("sec-websocket-version", "13")
            .body(Body::empty())
            .unwrap();
        assert_eq!(extract_token(&req), "");
    }

    #[test]
    fn set_headers_applies_all_provided_headers() {
        let builder = Request::builder().uri("/x");
        let mut headers: HashMap<HeaderName, HeaderValue> = HashMap::new();
        headers.insert(
            HeaderName::from_static("x-test"),
            HeaderValue::from_static("v"),
        );
        let req = set_headers(builder, headers).body(Body::empty()).unwrap();
        assert_eq!(req.headers().get("x-test").unwrap().to_str().unwrap(), "v");
    }

    #[test]
    fn prepare_route_request_uses_unknown_when_no_ip() {
        let h = HeaderMap::new();
        let uri = "/api/v1/foo".parse::<Uri>().unwrap();
        let req = prepare_route_request(&Method::GET, &h, &uri);
        let json = serde_json::to_string(&req).unwrap();
        assert!(json.contains("/api/v1/foo"));
        assert!(json.contains("unknown"));
        assert!(json.contains("GET"));
    }

    #[test]
    fn prepare_route_request_propagates_ip_from_headers() {
        let mut h = HeaderMap::new();
        h.insert("CF-Connecting-IP", "8.8.8.8".parse().unwrap());
        let uri = "/p".parse::<Uri>().unwrap();
        let req = prepare_route_request(&Method::POST, &h, &uri);
        let json = serde_json::to_string(&req).unwrap();
        assert!(json.contains("8.8.8.8"));
    }

    #[test]
    fn prepare_headers_includes_authorization_and_content_type() {
        let mut h = HeaderMap::new();
        h.insert(
            header::CONTENT_TYPE,
            HeaderValue::from_static("application/json"),
        );
        let result = prepare_headers(&h, "tok".to_string()).unwrap();
        assert_eq!(
            result.get(&header::AUTHORIZATION).unwrap().to_str().unwrap(),
            "tok"
        );
        assert_eq!(
            result.get(&header::CONTENT_TYPE).unwrap().to_str().unwrap(),
            "application/json"
        );
    }

    #[test]
    fn parse_url_builds_full_uri() {
        let resp_json = r#"{"serviceHost":"example.com","servicePort":8080}"#;
        let resp: AuthRouteResp = serde_json::from_str(resp_json).unwrap();
        let uri = "/path?x=1".parse::<Uri>().unwrap();
        let result = parse_url(resp, &uri, "http").unwrap();
        assert_eq!(result.to_string(), "http://example.com:8080/path?x=1");
    }
}
