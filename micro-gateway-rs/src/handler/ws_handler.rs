use axum::body::Body;
use axum::extract::WebSocketUpgrade;
use axum::extract::ws::WebSocket;
use axum::response::{IntoResponse, Response};
use futures_util::{SinkExt, StreamExt};
use hyper::Uri;
use tokio_tungstenite::connect_async;
use tokio_tungstenite::tungstenite::client::IntoClientRequest;
use tracing::{Instrument, Span, instrument};

use crate::constant;
use crate::exception::{ClientError, HandlerError};
use crate::proxy::{
    AuthorizedRoute, inject_trace_context, parse_url, principal_header_value, to_axum_message,
    to_tungstenite_message,
};

#[instrument(
    name = "proxy_websocket_request",
    skip(ws),
    fields(http.url = %uri.path())
)]
pub async fn ws_route_handler(
    ws: WebSocketUpgrade,
    uri: Uri,
    route: AuthorizedRoute,
) -> Result<Response<Body>, HandlerError> {
    let downstream_uri = Uri::builder()
        .path_and_query(uri.path())
        .build()
        .map_err(|error| ClientError::Request(error.to_string()))?;
    let target_uri = parse_url(&route, &downstream_uri, constant::WS)?;
    let principal = route.principal().clone();
    let span = Span::current();
    let response = ws
        .on_upgrade(|socket| proxy_websocket(socket, target_uri, principal).instrument(span))
        .into_response();

    let (parts, _) = response.into_parts();
    Ok(Response::from_parts(parts, Body::empty()))
}

async fn proxy_websocket(
    socket: WebSocket,
    target_uri: Uri,
    principal: crate::proxy::AuthPrincipal,
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

    let (downstream, response) = match connect_async(request).await {
        Ok(connection) => connection,
        Err(error) => {
            tracing::error!(%error, "failed to connect downstream WebSocket");
            return;
        }
    };
    tracing::info!(status = %response.status(), "downstream WebSocket connected");

    let (mut client_sender, mut client_receiver) = socket.split();
    let (mut downstream_sender, mut downstream_receiver) = downstream.split();

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
        _ = client_to_downstream => tracing::info!("client WebSocket connection closed"),
        _ = downstream_to_client => tracing::info!("downstream WebSocket connection closed"),
    }
}
