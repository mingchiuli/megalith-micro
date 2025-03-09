use axum::http::{HeaderName, HeaderValue};
use axum::{extract::Request, http::StatusCode, middleware::Next, response::Response};
use hyper::Uri;
use serde::Serialize;
use std::collections::HashMap;
use std::env;

use crate::exception::error::{ClientError, handle_api_error};
use crate::result::api_result::ApiResult;
use crate::{http::client, util::constant::AUTH_URL_KEY};

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct RouteCheckReq {
    method: String,
    route_mapping: String,
}

pub async fn process(req: Request, next: Next) -> Result<Response, StatusCode> {
    // Skip authentication for actuator endpoints
    if req.uri().path().starts_with("/actuator") {
        return Ok(next.run(req).await);
    }

    // Authenticate the request
    let (uri, req_body, headers) = extract_request_param(&req)?;
    if !auth(uri, req_body, headers).await? {
        return Err(StatusCode::FORBIDDEN);
    }

    Ok(next.run(req).await)
}

fn extract_request_param(
    req: &Request,
) -> Result<(Uri, RouteCheckReq, HashMap<HeaderName, HeaderValue>), ClientError> {
    // Extract data before async operations
    let method = req.method().to_string();
    let path = req.uri().path().to_string();

    // 获取认证令牌 - 根据协议选择不同的方式
        let auth_token = if req.headers().get("sec-websocket-version").is_some() ||
                          req.headers().get("upgrade").map(|h| h.to_str().unwrap_or("").to_lowercase() == "websocket").unwrap_or(false) {
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
        };

    let uri = build_auth_uri()?;
    let headers = build_headers(auth_token.as_str());
    let req_body = RouteCheckReq {
        method,
        route_mapping: path,
    };

    Ok((uri, req_body, headers))
}

async fn auth(
    uri: Uri,
    req_body: RouteCheckReq,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<bool, ClientError> {
    let resp: ApiResult<bool> = client::post(uri, req_body, headers)
        .await
        .map_err(handle_api_error)?;

    match resp.code() {
        200 => Ok(resp.into_data()),
        e => Err(ClientError::Response(e.to_string())),
    }
}

fn build_auth_uri() -> Result<Uri, ClientError> {
    let mut uri_str = env::var(AUTH_URL_KEY).map_err(|e| ClientError::Request(e.to_string()))?;
    uri_str.push_str("/auth/route/check");

    uri_str
        .parse::<Uri>()
        .map_err(|e| ClientError::Request(e.to_string()))
}

fn build_headers(auth_token: &str) -> HashMap<HeaderName, HeaderValue> {
    HashMap::from([
        (
            hyper::header::AUTHORIZATION,
            HeaderValue::from_str(auth_token).unwrap_or(HeaderValue::from_static("")),
        ),
        (
            hyper::header::CONTENT_TYPE,
            HeaderValue::from_static("application/json"),
        ),
    ])
}
