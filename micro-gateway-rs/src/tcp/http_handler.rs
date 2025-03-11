use std::{collections::HashMap, time::Duration};

use axum::{
    body::{Body, Bytes},
    extract::Request,
    http::{HeaderName, HeaderValue},
    response::Response,
};
use hyper::{Method, StatusCode, Uri};
use tokio::time::timeout;

use crate::{
    client::http_client::{self, AuthRouteResp},
    exception::error::{ClientError, handle_api_error},
    utils::http_util::{self},
};

const REQUEST_TIMEOUT: Duration = Duration::from_secs(5);

pub async fn handle_request(req: Request<Body>) -> Result<Response<Body>, StatusCode> {
    let uri = req.uri();
    // Extract authentication token
    let token = http_util::extract_token(&req);

    // Get authentication URL
    let auth_url = http_util::get_auth_url()?;

    // Prepare route request
    let req_body = http_util::prepare_route_request(req.method(), req.headers(), uri);

    // Forward to route service
    let route_resp = http_util::find_route(auth_url, req_body, &token).await?;

    // Prepare target request
    let target_uri = build_target_uri(uri, route_resp)?;

    // Forward to target service
    let response = forward_to_target_service(req, target_uri, token).await?;

    Ok(prepare_response(response)?)
}

async fn forward_to_target_service(
    req: Request<Body>,
    uri: hyper::Uri,
    token: String,
) -> Result<Response<Bytes>, ClientError> {
    let headers = prepare_headers(req.headers(), token)?;

    let resp = timeout(REQUEST_TIMEOUT, async {
        match *req.method() {
            Method::GET => http_client::get_raw(uri, headers)
                .await
                .map_err(handle_api_error),

            Method::POST => {
                let body = req.into_body();
                http_client::post_raw(uri, body, headers)
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

fn build_target_uri(uri: &Uri, route_resp: AuthRouteResp) -> Result<hyper::Uri, ClientError> {
    let path_and_query = uri
        .path_and_query()
        .ok_or_else(|| ClientError::Request("Invalid URI".to_string()))?
        .to_string();

    let uri = format!(
        "{}://{}:{}{}",
        "http",
        route_resp.service_host(),
        route_resp.service_port(),
        path_and_query
    );

    Ok(uri
        .parse::<hyper::Uri>()
        .map_err(|e| ClientError::Request(format!("parse: {}", e.to_string())))?)
}
