use axum::{
    extract::Request,
    http::{HeaderName, HeaderValue, request::Builder},
};
use hyper::{HeaderMap, Method, StatusCode, Uri};
use std::{collections::HashMap, env};

use crate::{
    client::http_client::{self, AuthRouteReq, AuthRouteResp},
    exception::error::{AuthError, ClientError},
    result::api_result::ApiResult,
    utils::constant::{
        AUTH_HEADER, AUTH_URL_KEY, CF_CONNECTING_IP, FORWARDED_HEADER, PROXY_CLIENT_IP,
        WL_PROXY_CLIENT_IP,
    },
};

use super::constant::UNKNOWN;

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
    let mut auth_url = env::var(AUTH_URL_KEY)
        .map_err(|_| AuthError::MissingConfig(format!("Missing {}", AUTH_URL_KEY)))?;

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

    let resp: ApiResult<AuthRouteResp> = http_client::post(auth_url, req_body, headers)
        .await
        .map_err(|e| ClientError::Status(StatusCode::BAD_GATEWAY.as_u16(), e.to_string()))?;
    Ok(resp.into_data())
}

pub fn prepare_route_request(method: &Method, headers: &HeaderMap, uri: &Uri) -> AuthRouteReq {
    let ip_addr = get_ip_from_headers(headers).unwrap_or_else(|| UNKNOWN.to_string());

    AuthRouteReq::new(method, uri.path().to_string(), ip_addr)
}
