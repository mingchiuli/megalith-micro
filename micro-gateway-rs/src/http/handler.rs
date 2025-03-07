use std::{collections::HashMap, time::Duration};

use axum::{
    body::{Body, Bytes},
    extract::Request,
    http::{HeaderName, HeaderValue},
    response::Response,
};
use hyper::{Method, StatusCode, Uri};
use tokio::time::timeout;

use super::client::{self};
use crate::{
    exception::error::{ClientError, handle_api_error},
    result::api_result::ApiResult,
    util::{
        constant::UNKNOWN,
        http_util::{self},
    },
};

use serde::{Deserialize, Serialize};

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct AuthRouteReq {
    method: String,
    route_mapping: String,
    ip_addr: String,
}

#[derive(Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
struct AuthRouteResp {
    service_host: String,
    service_port: u32,
}

impl AuthRouteResp {
    pub fn service_host(&self) -> &str {
        &self.service_host
    }

    pub fn service_port(&self) -> &u32 {
        &self.service_port
    }
}

const REQUEST_TIMEOUT: Duration = Duration::from_secs(10);

pub async fn handle_request(req: Request<Body>) -> Result<Response<Body>, StatusCode> {
    // Extract authentication token
    let token = http_util::extract_token(&req);

    // Get authentication URL
    let auth_url = http_util::get_auth_url()?;

    // Prepare auth request
    let req_body = prepare_auth_request(&req);

    // Forward to auth service
    let auth_resp = forward_to_auth_service(auth_url, req_body, &token).await?;

    // Prepare target request
    let target_uri = build_target_uri(&req, auth_resp)?;

    // Forward to target service
    let response = forward_to_target_service(req, target_uri, token).await?;

    Ok(prepare_response(response)?)
}

fn prepare_auth_request(req: &Request<Body>) -> AuthRouteReq {
    let ip_addr =
        http_util::get_ip_from_headers(req.headers()).unwrap_or_else(|| UNKNOWN.to_string());

    AuthRouteReq {
        method: req.method().to_string(),
        route_mapping: req.uri().path().to_string(),
        ip_addr,
    }
}

async fn forward_to_auth_service(
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

fn build_target_uri(
    req: &Request<Body>,
    auth_resp: AuthRouteResp,
) -> Result<hyper::Uri, ClientError> {
    let path_and_query = req
        .uri()
        .path_and_query()
        .ok_or_else(|| ClientError::Request("Invalid URI".to_string()))?
        .to_string();

    let uri = format!(
        "http://{}:{}{}",
        auth_resp.service_host(),
        auth_resp.service_port(),
        path_and_query
    );

    Ok(uri
        .parse::<hyper::Uri>()
        .map_err(|e| ClientError::Request(format!("parse: {}", e.to_string())))?)
}

async fn forward_to_target_service(
    req: Request<Body>,
    uri: hyper::Uri,
    token: String,
) -> Result<Response<Bytes>, ClientError> {
    let headers = prepare_headers(req.headers(), token)?;

    let resp = timeout(REQUEST_TIMEOUT, async {
        match *req.method() {
            Method::GET => client::get_raw(uri, headers).await.map_err(handle_api_error),

            Method::POST => {
                let body = req.into_body();
                client::post_raw(uri, body, headers)
                    .await
                    .map_err(handle_api_error)
            }
            _ => Err(ClientError::Status(
                StatusCode::METHOD_NOT_ALLOWED.as_u16(),
                String::from("Method Not Allowed"),
            )),
        }
    })
    .await
    .map_err(|e| ClientError::Status(StatusCode::BAD_GATEWAY.as_u16(), e.to_string()))??;

    Ok(resp)
}

fn prepare_headers(
    req_headers: &hyper::HeaderMap,
    token: String,
) -> std::result::Result<HashMap<HeaderName, HeaderValue>, ClientError> {
    let mut headers = HashMap::new();

    headers.insert(
        hyper::header::AUTHORIZATION,
        HeaderValue::from_str(&token).unwrap_or(HeaderValue::from_static("")),
    );

    let content_type = req_headers
        .get(hyper::header::CONTENT_TYPE)
        .unwrap_or(&HeaderValue::from_static("application/json"))
        .to_str()
        .map_err(|e| ClientError::Request(e.to_string()))?
        .to_string();

    headers.insert(
        hyper::header::CONTENT_TYPE,
        HeaderValue::from_str(&content_type)
            .unwrap_or(HeaderValue::from_static("application/json")),
    );

    Ok(headers)
}

fn prepare_response(resp: Response<Bytes>) -> Result<Response<Body>, ClientError> {
    let content_type = resp
        .headers()
        .get(hyper::header::CONTENT_TYPE)
        .unwrap_or(&HeaderValue::from_static("application/json"))
        .to_str()
        .map_err(|e| ClientError::Response(e.to_string()))?
        .to_string();

    let mut builder = Response::builder().status(resp.status());

    if content_type == "application/octet-stream" {
        builder = builder.header(hyper::header::CONTENT_TYPE, "application/octet-stream");
    } else {
        builder = builder.header(hyper::header::CONTENT_TYPE, "application/json");
    }

    Ok(builder
        .body(Body::from(resp.into_body()))
        .map_err(|e| ClientError::Response(e.to_string()))?)
}
