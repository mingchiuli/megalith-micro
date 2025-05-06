use std::time::Duration;

use axum::{
    body::{Body, Bytes},
    extract::Request,
    response::Response,
};
use hyper::{Method, StatusCode};
use tokio::time::timeout;

use crate::{
    client::http_client::{self},
    exception::error::{handle_api_error, ClientError, HandlerError},
    utils::http_util::{self},
};

const REQUEST_TIMEOUT: Duration = Duration::from_secs(10);

pub async fn handle_request(req: Request<Body>) -> Result<Response<Body>, HandlerError> {
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
    let target_uri = http_util::parse_url(route_resp, uri, "http")?;

    // Forward to target service
    let response = forward_to_target_service(req, target_uri, token).await?;

    Ok(http_util::prepare_response(response)?)
}

async fn forward_to_target_service(
    req: Request<Body>,
    uri: hyper::Uri,
    token: String,
) -> Result<Response<Bytes>, ClientError> {
    let headers = http_util::prepare_headers(req.headers(), token)?;

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
