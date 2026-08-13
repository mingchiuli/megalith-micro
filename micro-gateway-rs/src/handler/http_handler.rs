use axum::{body::Body, extract::Request, response::Response};
use hyper::StatusCode;
use std::time::Duration;
use tokio::time::timeout;
use tracing::instrument;

use crate::{
    client::{self, AuthRouteResp, HttpClient},
    constant,
    exception::{ClientError, HandlerError, handle_api_error},
    utils,
};

const REQUEST_TIMEOUT: Duration = Duration::from_secs(300);

#[instrument(name = "proxy_http_request", skip(req))]
pub async fn handle_request(
    client: &HttpClient,
    req: Request<Body>,
    route: AuthRouteResp,
) -> Result<Response<Body>, HandlerError> {
    let response = forward_to_target_service(client, route, req).await?;

    Ok(utils::prepare_response(response))
}

#[tracing::instrument(
    name = "forward_to_backend",
    skip(client, req),
    fields(
        auth.url = %req.uri(),
        otel.kind = "client",
        peer.service = %route_resp.service_host(),
        server.address = tracing::field::Empty
    )
)]
async fn forward_to_target_service(
    client: &HttpClient,
    route_resp: AuthRouteResp,
    req: Request<Body>,
) -> Result<Response<hyper::body::Incoming>, ClientError> {
    let target_uri = utils::parse_url(&route_resp, req.uri(), constant::HTTP)?;

    let headers = utils::prepare_headers(req.headers(), route_resp.principal(), req.uri().path())?;
    let method = req.method().clone();
    let body = req.into_body();

    let resp = timeout(
        REQUEST_TIMEOUT,
        client::request_raw(client, method, target_uri, body, headers),
    )
    .await
    .map_err(|e| ClientError::Status(StatusCode::BAD_GATEWAY.as_u16(), e.to_string()))?
    .map_err(handle_api_error)?;

    Ok(resp)
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::{
        Router, body,
        extract::ConnectInfo,
        routing::{any, get},
    };
    use bytes::Bytes;
    use futures_util::stream;
    use http_body_util::BodyExt;
    use hyper::Method;
    use std::{convert::Infallible, net::SocketAddr};
    use tokio::time::{Duration, sleep, timeout};

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
            "servicePort": port,
            "principal": {"userId": 42, "roles": ["user"]}
        }))
        .unwrap()
    }

    async fn spawn(app: Router) -> (u16, tokio::task::JoinHandle<()>) {
        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let port = listener.local_addr().unwrap().port();
        let server = tokio::spawn(async move {
            axum::serve(listener, app).await.unwrap();
        });
        (port, server)
    }

    #[tokio::test]
    async fn forwards_original_method_query_and_body() {
        let (port, server) = spawn(Router::new().route("/{*wildcard}", any(echo_request))).await;
        let state = client::GatewayState::new();

        for method in [Method::DELETE, Method::PATCH] {
            let request = Request::builder()
                .method(method.clone())
                .uri("/resource?force=true")
                .header(hyper::header::CONTENT_TYPE, "application/json")
                .body(Body::from(r#"{"id":7}"#))
                .unwrap();

            let response = forward_to_target_service(state.client(), route(port), request)
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

    #[tokio::test]
    async fn reuses_a_pooled_connection_for_the_same_backend() {
        async fn peer_port(ConnectInfo(addr): ConnectInfo<SocketAddr>) -> String {
            addr.port().to_string()
        }

        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let port = listener.local_addr().unwrap().port();
        let app = Router::new().route("/peer", get(peer_port));
        let server = tokio::spawn(async move {
            axum::serve(
                listener,
                app.into_make_service_with_connect_info::<SocketAddr>(),
            )
            .await
            .unwrap();
        });
        let state = client::GatewayState::new();

        let mut observed = Vec::new();
        for _ in 0..2 {
            let request = Request::builder().uri("/peer").body(Body::empty()).unwrap();
            let response = forward_to_target_service(state.client(), route(port), request)
                .await
                .unwrap();
            observed.push(
                String::from_utf8(
                    response
                        .into_body()
                        .collect()
                        .await
                        .unwrap()
                        .to_bytes()
                        .to_vec(),
                )
                .unwrap(),
            );
        }

        assert_eq!(observed[0], observed[1]);
        server.abort();
    }

    #[tokio::test]
    async fn streams_response_before_the_backend_finishes() {
        async fn delayed_body() -> Body {
            let chunks = stream::unfold(0, |index| async move {
                match index {
                    0 => Some((Ok::<_, Infallible>(Bytes::from_static(b"first")), 1)),
                    1 => {
                        sleep(Duration::from_millis(250)).await;
                        Some((Ok(Bytes::from_static(b"second")), 2))
                    }
                    _ => None,
                }
            });
            Body::from_stream(chunks)
        }

        let (port, server) = spawn(Router::new().route("/stream", get(delayed_body))).await;
        let state = client::GatewayState::new();
        let request = Request::builder()
            .uri("/stream")
            .body(Body::empty())
            .unwrap();
        let response = forward_to_target_service(state.client(), route(port), request)
            .await
            .unwrap();
        let mut body = response.into_body();

        let first = timeout(Duration::from_millis(100), body.frame())
            .await
            .expect("first chunk should not wait for the full response")
            .unwrap()
            .unwrap()
            .into_data()
            .unwrap();
        assert_eq!(first, Bytes::from_static(b"first"));

        server.abort();
    }

    #[tokio::test]
    async fn streams_request_without_buffering_the_full_body() {
        async fn first_chunk(mut request: Request<Body>) -> Bytes {
            request
                .body_mut()
                .frame()
                .await
                .unwrap()
                .unwrap()
                .into_data()
                .unwrap()
        }

        let (port, server) = spawn(Router::new().route("/upload", any(first_chunk))).await;
        let chunks = stream::unfold(0, |index| async move {
            match index {
                0 => Some((Ok::<_, Infallible>(Bytes::from_static(b"first")), 1)),
                1 => {
                    sleep(Duration::from_millis(250)).await;
                    Some((Ok(Bytes::from_static(b"second")), 2))
                }
                _ => None,
            }
        });
        let request = Request::builder()
            .method(Method::POST)
            .uri("/upload")
            .body(Body::from_stream(chunks))
            .unwrap();
        let state = client::GatewayState::new();

        let response = timeout(
            Duration::from_millis(100),
            forward_to_target_service(state.client(), route(port), request),
        )
        .await
        .expect("backend should receive the first chunk before the request is complete")
        .unwrap();
        let body = response.into_body().collect().await.unwrap().to_bytes();
        assert_eq!(body, Bytes::from_static(b"first"));

        server.abort();
    }
}
