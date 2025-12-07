use axum::{
    BoxError,
    body::{Body, Bytes},
    extract::Request,
    response::Response,
};
use http_body_util::combinators::BoxBody;
use hyper::{Method, StatusCode};
use std::time::Duration;
use tokio::time::timeout;
use tracing::instrument;

use crate::{
    client::{self, AuthRouteResp},
    constant,
    exception::{ClientError, HandlerError, handle_api_error},
    utils,
};

const REQUEST_TIMEOUT: Duration = Duration::from_secs(30);

#[instrument(name = "proxy_http_request", skip(req))]
pub async fn handle_request(req: Request<Body>) -> Result<Response<Body>, HandlerError> {
    // Extract authentication token
    let token = utils::extract_token(&req);

    // Get authentication URL
    let auth_url = utils::get_auth_url()?;

    // Prepare route request
    let req_body = utils::prepare_route_request(req.method(), req.headers(), req.uri());

    // Forward to route service
    let route_resp = utils::find_route(auth_url, req_body, &token).await?;

    // Forward to target service
    let response = forward_to_target_service(route_resp, req, token).await?;

    Ok(utils::prepare_response(response)?)
}

#[tracing::instrument(
    name = "forward_to_backend",
    skip(req, token),
    fields(
        auth.url = %req.uri(),
        otel.kind = "client",
        peer.service = %route_resp.service_host(),
        server.address = tracing::field::Empty
    )
)]
async fn forward_to_target_service(
    route_resp: AuthRouteResp,
    req: Request<Body>,
    token: String,
) -> Result<Response<BoxBody<Bytes, BoxError>>, ClientError> {
    let target_uri = utils::parse_url(route_resp, req.uri(), constant::HTTP)?;

    let mut headers = utils::prepare_headers(req.headers(), token)?;

    // 注入 Trace Context
    utils::inject_trace_context_hashmap(&mut headers);

    let resp = timeout(REQUEST_TIMEOUT, async {
        match *req.method() {
            Method::GET => client::get_raw(target_uri, headers)
                .await
                .map_err(handle_api_error),
            Method::POST => {
                let body = req.into_body();
                client::post_raw(target_uri, body, headers)
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
