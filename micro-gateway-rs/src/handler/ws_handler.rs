use axum::body::Body;
use axum::extract::WebSocketUpgrade;
use axum::extract::ws::{CloseFrame, Message, WebSocket};
use axum::response::{IntoResponse, Response};
use futures_util::{SinkExt, StreamExt};
use hyper::Uri;
use std::time::{Duration, SystemTime, UNIX_EPOCH};
use tokio::time::{Instant, timeout, timeout_at};
use tokio_tungstenite::connect_async;
use tokio_tungstenite::tungstenite::client::IntoClientRequest;
use tracing::{Instrument, Span, instrument};

use crate::constant;
use crate::exception::{ClientError, HandlerError};
use crate::proxy::{
    AuthorizedRoute, inject_trace_context, parse_url, principal_header_value, to_axum_message,
    to_tungstenite_message,
};

const CLOSE_DOWNSTREAM_UNAVAILABLE: u16 = 4503;
const DOWNSTREAM_UNAVAILABLE_REASON: &str = "downstream WebSocket unavailable";

#[instrument(
    name = "proxy_websocket_request",
    skip(ws, route, uri),
    fields(http.url = %uri.path())
)]
pub async fn ws_route_handler(
    ws: WebSocketUpgrade,
    uri: Uri,
    route: AuthorizedRoute,
) -> Result<Response<Body>, HandlerError> {
    let deadline = authorization_deadline(route.authorization_expires_at())?;
    let downstream_uri = Uri::builder()
        .path_and_query(uri.path())
        .build()
        .map_err(|error| ClientError::Request(error.to_string()))?;
    let target_uri = parse_url(&route, &downstream_uri, constant::WS)?;
    let principal = route.principal().clone();
    let span = Span::current();
    let response = ws
        .on_upgrade(move |socket| {
            proxy_websocket(socket, target_uri, principal, deadline).instrument(span)
        })
        .into_response();

    let (parts, _) = response.into_parts();
    Ok(Response::from_parts(parts, Body::empty()))
}

async fn proxy_websocket(
    mut socket: WebSocket,
    target_uri: Uri,
    principal: crate::proxy::AuthPrincipal,
    deadline: Instant,
) {
    let mut request = match target_uri.into_client_request() {
        Ok(request) => request,
        Err(error) => {
            tracing::error!(%error, "failed to build downstream WebSocket request");
            return;
        }
    };

    inject_trace_context(request.headers_mut());
    match principal_header_value(&principal) {
        Ok(value) => {
            request
                .headers_mut()
                .insert(crate::proxy::PRINCIPAL_HEADER, value);
        }
        Err(error) => {
            tracing::error!(%error, "failed to encode WebSocket principal");
            return;
        }
    }

    let (downstream, response) = match timeout_at(deadline, connect_async(request)).await {
        Ok(Ok(connection)) => connection,
        result => {
            tracing::warn!(
                ?result,
                "failed to connect downstream WebSocket before authorization deadline"
            );
            let _ = socket
                .send(Message::Close(Some(if Instant::now() >= deadline {
                    expired_close_frame()
                } else {
                    downstream_unavailable_close_frame()
                })))
                .await;
            return;
        }
    };
    tracing::info!(status = %response.status(), "downstream WebSocket connected");

    let (mut client_sender, mut client_receiver) = socket.split();
    let (mut downstream_sender, mut downstream_receiver) = downstream.split();

    let expired = {
        let client_to_downstream = async {
            while let Some(message) = client_receiver.next().await {
                match message {
                    Ok(message) => {
                        if let Err(error) = downstream_sender
                            .send(to_tungstenite_message(message))
                            .await
                        {
                            tracing::error!(%error, "failed to forward WebSocket message downstream");
                            break;
                        }
                    }
                    Err(error) => {
                        tracing::error!(%error, "failed to receive client WebSocket message");
                        break;
                    }
                }
            }
        };

        let downstream_to_client = async {
            while let Some(message) = downstream_receiver.next().await {
                match message {
                    Ok(message) => {
                        let Some(message) = to_axum_message(message) else {
                            continue;
                        };
                        if let Err(error) = client_sender.send(message).await {
                            tracing::error!(%error, "failed to forward WebSocket message to client");
                            break;
                        }
                    }
                    Err(error) => {
                        tracing::error!(%error, "failed to receive downstream WebSocket message");
                        break;
                    }
                }
            }
        };

        tokio::select! {
            biased;
            _ = tokio::time::sleep_until(deadline) => true,
            _ = client_to_downstream => false,
            _ = downstream_to_client => false,
        }
    };
    if expired {
        let _ = timeout(
            Duration::from_secs(1),
            client_sender.send(Message::Close(Some(expired_close_frame()))),
        )
        .await;
    }
}

fn authorization_deadline(expires_at: Option<u64>) -> Result<Instant, ClientError> {
    let now = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as u64;
    let remaining = expires_at
        .and_then(|expiry| expiry.checked_sub(now))
        .filter(|remaining| *remaining > 0 && *remaining <= 60_000)
        .ok_or_else(|| {
            ClientError::Status(401, "invalid WebSocket authorization deadline".into())
        })?;
    Ok(Instant::now() + Duration::from_millis(remaining))
}

fn expired_close_frame() -> CloseFrame {
    CloseFrame {
        code: 4401,
        reason: "authorization expired".into(),
    }
}

fn downstream_unavailable_close_frame() -> CloseFrame {
    CloseFrame {
        code: CLOSE_DOWNSTREAM_UNAVAILABLE,
        reason: DOWNSTREAM_UNAVAILABLE_REASON.into(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn rejects_missing_expired_and_unbounded_authorization() {
        let now = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_millis() as u64;
        assert!(authorization_deadline(None).is_err());
        assert!(authorization_deadline(Some(now - 1)).is_err());
        assert!(authorization_deadline(Some(now + 120_000)).is_err());
        assert!(authorization_deadline(Some(now + 5_000)).is_ok());
    }

    #[tokio::test]
    async fn raw_client_is_closed_at_the_original_deadline_without_client_cooperation() {
        use axum::{Router, routing::get};
        let backend_listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let backend_port = backend_listener.local_addr().unwrap().port();
        let backend = tokio::spawn(async move {
            axum::serve(
                backend_listener,
                Router::new().route(
                    "/room",
                    get(|ws: WebSocketUpgrade| async {
                        ws.on_upgrade(|mut socket| async move {
                            while let Some(Ok(message)) = socket.next().await {
                                if socket.send(message).await.is_err() {
                                    break;
                                }
                            }
                        })
                    }),
                ),
            )
            .await
            .unwrap();
        });
        let gateway_listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let gateway_port = gateway_listener.local_addr().unwrap().port();
        let expires = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_millis() as u64
            + 300;
        let gateway = tokio::spawn(async move {
            axum::serve(
                gateway_listener,
                Router::new().route(
                    "/room",
                    get(move |ws: WebSocketUpgrade, uri: Uri| async move {
                        let route = serde_json::from_value(serde_json::json!({
                    "serviceHost": "127.0.0.1", "servicePort": backend_port,
                    "principal": {"userId": 42, "roles": []}, "authorizationExpiresAt": expires
                })).unwrap();
                        ws_route_handler(ws, uri, route).await
                    }),
                ),
            )
            .await
            .unwrap();
        });
        let url = format!("ws://127.0.0.1:{gateway_port}/room");
        let (mut socket, _) = connect_async(&url).await.unwrap();
        socket
            .send(tokio_tungstenite::tungstenite::Message::Text(
                "hello".into(),
            ))
            .await
            .unwrap();
        assert_eq!(
            socket.next().await.unwrap().unwrap().into_text().unwrap(),
            "hello"
        );
        let closed = timeout(Duration::from_secs(2), socket.next())
            .await
            .unwrap()
            .unwrap()
            .unwrap();
        match closed {
            tokio_tungstenite::tungstenite::Message::Close(Some(frame)) => {
                assert_eq!(u16::from(frame.code), 4401)
            }
            other => panic!("expected authorization close, got {other:?}"),
        }
        assert!(
            connect_async(&url).await.is_err(),
            "replaying the expired lease cannot open another connection"
        );
        gateway.abort();
        backend.abort();
    }

    #[tokio::test]
    async fn a_stalled_downstream_handshake_cannot_outlive_authorization() {
        use axum::{Router, routing::get};
        let stalled = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let target: Uri = format!(
            "ws://127.0.0.1:{}/room",
            stalled.local_addr().unwrap().port()
        )
        .parse()
        .unwrap();
        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let port = listener.local_addr().unwrap().port();
        let server = tokio::spawn(async move {
            axum::serve(
                listener,
                Router::new().route(
                    "/room",
                    get(move |ws: WebSocketUpgrade| {
                        let target = target.clone();
                        async move {
                            let principal =
                                serde_json::from_value(serde_json::json!({"userId":42,"roles":[]}))
                                    .unwrap();
                            let deadline = Instant::now() + Duration::from_millis(100);
                            ws.on_upgrade(move |socket| {
                                proxy_websocket(socket, target, principal, deadline)
                            })
                        }
                    }),
                ),
            )
            .await
            .unwrap();
        });
        let (mut client, _) = connect_async(format!("ws://127.0.0.1:{port}/room"))
            .await
            .unwrap();
        let close = timeout(Duration::from_secs(2), client.next())
            .await
            .unwrap()
            .unwrap()
            .unwrap();
        match close {
            tokio_tungstenite::tungstenite::Message::Close(Some(frame)) => {
                assert_eq!(u16::from(frame.code), 4401)
            }
            other => panic!("expected authorization close, got {other:?}"),
        }
        server.abort();
        drop(stalled);
    }

    #[test]
    fn downstream_connect_failure_uses_transient_close_code() {
        let frame = downstream_unavailable_close_frame();

        assert_eq!(frame.code, 4503);
        assert_eq!(frame.reason, DOWNSTREAM_UNAVAILABLE_REASON);
    }
}
