use axum::{
    BoxError,
    body::{Body, Bytes},
    http::{HeaderName, HeaderValue, request::Builder},
    response::Response,
};

use http_body_util::combinators::BoxBody;

use hyper::{HeaderMap, Method, StatusCode, Uri, header};
use std::collections::HashMap;

use crate::{
    client::{self, AuthRouteReq, AuthRouteResp},
    config::{self, ConfigKey},
    constant::UNKNOWN,
    exception::{ClientError, handle_api_error},
    result::ApiResult,
};

use opentelemetry::global;
use opentelemetry_http::HeaderInjector;
use tracing_opentelemetry::OpenTelemetrySpanExt;

use super::request_metadata::{REFRESH_TOKEN_COOKIE, cookie_value, get_ip_from_headers};

const REFRESH_TOKEN_PATH: &str = "/token/refresh";

pub fn get_auth_url() -> Result<hyper::Uri, ClientError> {
    let mut auth_url = config::get_config(ConfigKey::AuthUrlKey);

    auth_url.push_str("/auth/route");
    auth_url
        .parse::<hyper::Uri>()
        .map_err(|e| ClientError::Request(format!("invalid auth URL: {e}")))
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
        .map_err(handle_api_error)?;
    if resp.code() != i32::from(StatusCode::OK.as_u16()) {
        return Err(ClientError::Status(
            StatusCode::BAD_GATEWAY.as_u16(),
            format!("auth service returned body code {}", resp.code()),
        ));
    }
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

    uri.parse::<hyper::Uri>()
        .map_err(|e| ClientError::Request(format!("parse: {e}")))
}

pub fn prepare_headers(
    req_headers: &hyper::HeaderMap,
    token: String,
    request_path: &str,
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

    if request_path == REFRESH_TOKEN_PATH
        && let Some(refresh_token) = cookie_value(req_headers, REFRESH_TOKEN_COOKIE)
    {
        let cookie = format!("{REFRESH_TOKEN_COOKIE}={refresh_token}");
        headers.insert(
            header::COOKIE,
            HeaderValue::from_str(&cookie)
                .map_err(|e| ClientError::Request(format!("invalid refresh cookie: {e}")))?,
        );
    }

    Ok(headers)
}

pub fn prepare_response(
    resp: Response<BoxBody<Bytes, BoxError>>,
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

    builder
        .body(axum_body)
        .map_err(|e| ClientError::Response(e.to_string()))
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
    use axum::{body::Body, extract::Request};

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
    fn prepare_headers_for_refresh_forwards_only_the_refresh_cookie() {
        let mut h = HeaderMap::new();
        h.insert(
            header::CONTENT_TYPE,
            HeaderValue::from_static("application/json"),
        );
        h.insert(
            header::COOKIE,
            HeaderValue::from_static(
                "other=1; megalith_access_token=access-jwt; megalith_refresh_token=refresh-jwt",
            ),
        );
        let result = prepare_headers(&h, "tok".to_string(), "/token/refresh").unwrap();
        assert_eq!(
            result
                .get(&header::AUTHORIZATION)
                .unwrap()
                .to_str()
                .unwrap(),
            "tok"
        );
        assert_eq!(
            result.get(&header::CONTENT_TYPE).unwrap().to_str().unwrap(),
            "application/json"
        );
        assert_eq!(
            result.get(&header::COOKIE).unwrap().to_str().unwrap(),
            "megalith_refresh_token=refresh-jwt"
        );
    }

    #[test]
    fn prepare_headers_does_not_forward_cookies_to_business_routes() {
        let mut h = HeaderMap::new();
        h.insert(
            header::COOKIE,
            HeaderValue::from_static(
                "megalith_access_token=access-jwt; megalith_refresh_token=refresh-jwt",
            ),
        );

        let result = prepare_headers(&h, "Bearer access-jwt".to_string(), "/blog/list").unwrap();

        assert!(!result.contains_key(&header::COOKIE));
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
