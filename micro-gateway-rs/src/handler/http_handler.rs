use axum::{
    BoxError,
    body::{Body, Bytes},
    extract::Request,
    response::Response,
};
use http_body_util::combinators::BoxBody;
use hyper::StatusCode;
use std::time::Duration;
use tokio::time::timeout;
use tracing::instrument;

use crate::{
    client::{self, AuthRouteResp},
    constant,
    exception::{ClientError, HandlerError, handle_api_error},
    utils,
};

const REQUEST_TIMEOUT: Duration = Duration::from_secs(300);

#[instrument(name = "proxy_http_request", skip(req))]
pub async fn handle_request(
    req: Request<Body>,
    route: AuthRouteResp,
) -> Result<Response<Body>, HandlerError> {
    // Extract authentication token
    let token = utils::extract_token(&req);

    // Forward to target service
    let response = forward_to_target_service(route, req, token).await?;

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

    let headers = utils::prepare_headers(req.headers(), token, req.uri().path())?;
    let method = req.method().clone();
    let body = req.into_body();

    let resp = timeout(
        REQUEST_TIMEOUT,
        client::request_raw(method, target_uri, Some(body), headers),
    )
    .await
    .map_err(|e| ClientError::Status(StatusCode::BAD_GATEWAY.as_u16(), e.to_string()))?
    .map_err(handle_api_error)?;

    Ok(resp)
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::Router;
    use axum::body;
    use axum::routing::any;
    use http_body_util::BodyExt;
    use hyper::Method;

    async fn echo_request(req: Request<Body>) -> String {
        let method = req.method().clone();
        let path_and_query = req
            .uri()
            .path_and_query()
            .map(|value| value.as_str())
            .unwrap_or(req.uri().path())
            .to_string();
        let body = body::to_bytes(req.into_body(), usize::MAX).await.unwrap();
        format!(
            "{method}|{path_and_query}|{}",
            String::from_utf8_lossy(&body)
        )
    }

    fn route(port: u16) -> AuthRouteResp {
        serde_json::from_value(serde_json::json!({
            "serviceHost": "127.0.0.1",
            "servicePort": port
        }))
        .unwrap()
    }

    #[tokio::test]
    async fn forwards_original_method_query_and_body() {
        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let port = listener.local_addr().unwrap().port();
        let server = tokio::spawn(async move {
            axum::serve(
                listener,
                Router::new().route("/{*wildcard}", any(echo_request)),
            )
            .await
            .unwrap();
        });

        for method in [Method::DELETE, Method::PATCH] {
            let request = Request::builder()
                .method(method.clone())
                .uri("/resource?force=true")
                .header(hyper::header::CONTENT_TYPE, "application/json")
                .body(Body::from(r#"{"id":7}"#))
                .unwrap();

            let response = forward_to_target_service(route(port), request, String::new())
                .await
                .unwrap();
            let response_body = response.into_body().collect().await.unwrap().to_bytes();

            assert_eq!(
                String::from_utf8_lossy(&response_body),
                format!(r#"{method}|/resource?force=true|{{"id":7}}"#)
            );
        }

        server.abort();
    }
}
