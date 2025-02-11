use axum::http::HeaderValue;
use axum::{extract::Request, http::StatusCode, middleware::Next, response::Response};
use hyper::Uri;
use serde::Serialize;
use std::collections::HashMap;

use crate::{http::client, util::constant::AUTH_URL_KEY, util::http_util::ApiResult};
use std::env;

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct RouteCheckReq {
    method: String,
    route_mapping: String,
}

pub async fn process(req: Request, next: Next) -> Result<Response, StatusCode> {
    // Get auth service URL from environment
    if req.uri().path().starts_with("/actuator") {
        return Ok(next.run(req).await);
    }

    let mut uri_str = env::var(AUTH_URL_KEY).map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;
    uri_str.push_str("/auth/route/check");

    let uri = uri_str
        .parse::<Uri>()
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    // Extract request details
    let method = req.method().as_str().to_string();
    let route_mapping = format!("{}", req.uri().path());

    // Extract and validate authorization token
    let token = req
        .headers()
        .get("Authorization")
        .and_then(|h| h.to_str().ok())
        .unwrap_or("");

    // Prepare headers for auth request
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

    // Prepare and send auth request
    let req_body = RouteCheckReq {
        method,
        route_mapping,
    };

    // Make auth request and handle response
    let resp: ApiResult<bool> = client::post(&uri, req_body, headers)
        .await
        .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

    // Check response status
    match resp.code() {
        200 => {
            if !*resp.data() {
                return Err(StatusCode::FORBIDDEN);
            }
        }
        _ => return Err(StatusCode::INTERNAL_SERVER_ERROR),
    }

    // Continue with the request if authorized
    let resp = next.run(req).await;
    Ok(resp)
}
