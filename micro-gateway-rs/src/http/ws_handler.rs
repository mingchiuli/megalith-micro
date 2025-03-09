use axum::extract::ws::{Message as AxumMessage, WebSocket};
use axum::extract::WebSocketUpgrade;
use axum::response::IntoResponse;
use futures_util::{SinkExt, StreamExt};
use hyper::{HeaderMap, Method, StatusCode, Uri};
use tokio_tungstenite::connect_async;
use tokio_tungstenite::tungstenite::Message as TungsteniteMessage;
use url::Url;

use crate::exception::error::ClientError;
use crate::util::http_util::{self};

use super::client::AuthRouteResp;

pub async fn ws_route_handler(ws: WebSocketUpgrade, uri: Uri) -> impl IntoResponse {
    log::info!("WebSocket connection establishing：{}", uri);
    
    // Extract authentication token
    let token = extract_token(&uri);

    // Get authentication URL
    let auth_url = match http_util::get_auth_url() {
        Ok(url) => url,
        Err(e) => {
            log::error!("Failed to get auth URL: {}", e);
            return StatusCode::INTERNAL_SERVER_ERROR.into_response();
        }
    };

    // Prepare auth request
    let req_body = http_util::prepare_route_request(&Method::GET, &HeaderMap::new(), &uri);

    // Forward to auth service
    let route_resp = http_util::find_route(auth_url, req_body, &token)
        .await
        .unwrap();

    let mut new_url = parse_url(route_resp, &uri);

    // Set the path from the original request
    new_url.set_path(uri.path());

    // Set the query parameters if they exist
    if let Some(q) = uri.query() {
        new_url.set_query(Some(q));
    }

    log::info!("Forwarding WebSocket connection to: {}", new_url);

    ws.on_upgrade(|socket| async move {
        handle_websocket_request(socket, new_url).await;
    })
}

fn parse_url(route_resp: AuthRouteResp, uri: &Uri) -> Url {
    let path_and_query = uri
        .path_and_query()
        .ok_or_else(|| ClientError::Request("Invalid URI".to_string()))
        .unwrap()
        .to_string();

    let uri = format!(
        "{}://{}:{}{}",
        "ws",
        route_resp.service_host(),
        route_resp.service_port(),
        path_and_query
    );

    url::Url::parse(&uri).unwrap()
}

fn extract_token(uri: &Uri) -> String {
    uri.query()
        .and_then(|q| {
            url::form_urlencoded::parse(q.as_bytes())
                .find(|(key, _)| key == "token")
                .map(|(_, value)| value.to_string())
        })
        .unwrap_or_default()
}

async fn handle_websocket_request(ws: WebSocket, target_url: Url) {
    log::info!(
        "WebSocket connection established, forwarding to: {}",
        target_url
    );

    // 连接到微服务
    log::info!("Connecting to micro-websocket service at: {}", target_url);
    let (server_ws, _) = match connect_async(target_url.as_str()).await {
        Ok(conn) => conn,
        Err(e) => {
            log::error!("Failed to connect to websocket service: {}", e);
            return;
        }
    };

    // 使用 futures_util 的 split 方法
    let (mut client_sender, mut client_receiver) = futures_util::stream::StreamExt::split(ws);
    let (mut server_sender, mut server_receiver) =
        futures_util::stream::StreamExt::split(server_ws);

    // 从客户端到服务端的消息转发
    let client_to_server = async {
        while let Some(msg) = client_receiver.next().await {
            match msg {
                Ok(msg) => {
                    // 将 axum WebSocket 消息转换为 tungstenite 消息
                    let converted_msg = match msg {
                        AxumMessage::Text(text) => TungsteniteMessage::text(text.as_str()),
                        AxumMessage::Binary(data) => TungsteniteMessage::binary(data.to_vec()),
                        AxumMessage::Ping(data) => TungsteniteMessage::Ping(data.clone().into()),
                        AxumMessage::Pong(data) => TungsteniteMessage::Pong(data.clone().into()),
                        AxumMessage::Close(Some(frame)) => {
                            // Convert from u16 to CloseCode
                            let code = tokio_tungstenite::tungstenite::protocol::frame::coding::CloseCode::from(frame.code);

                            // If there's a way to create a CloseFrame from a reason string
                            let reason_string = frame.reason.as_str().to_string();

                            // Try using a builder pattern or other constructor
                            // Without seeing the full API, we can attempt a few approaches:

                            // Option 1: If there's a with_reason or similar method
                            let close_frame =
                                tokio_tungstenite::tungstenite::protocol::CloseFrame {
                                    code,
                                    reason: reason_string.into(), // Try with bytes directly
                                };

                            tokio_tungstenite::tungstenite::Message::Close(Some(close_frame))
                        }
                        AxumMessage::Close(None) => TungsteniteMessage::Close(None),
                    };

                    if let Err(e) = server_sender.send(converted_msg).await {
                        log::error!("Error forwarding message to server: {}", e);
                        break;
                    }
                }
                Err(e) => {
                    log::error!("Error receiving message from client: {}", e);
                    break;
                }
            }
        }
    };

    // 从服务端到客户端的消息转发
    let server_to_client = async {
        while let Some(msg) = server_receiver.next().await {
            match msg {
                Ok(msg) => {
                    // 将 tungstenite 消息转换为 axum WebSocket 消息
                    let converted_msg = match msg {
                        TungsteniteMessage::Text(text) => {
                            // 将 String 转换为 axum 的 Utf8Bytes
                            AxumMessage::Text(axum::extract::ws::Utf8Bytes::from(text.as_str()))
                        }
                        TungsteniteMessage::Binary(data) => {
                            // 从 Vec<u8> 转为 axum 的 Bytes
                            AxumMessage::Binary(axum::body::Bytes::from(data))
                        }
                        TungsteniteMessage::Ping(data) => {
                            AxumMessage::Ping(axum::body::Bytes::from(data))
                        }
                        TungsteniteMessage::Pong(data) => {
                            AxumMessage::Pong(axum::body::Bytes::from(data))
                        }
                        TungsteniteMessage::Close(frame) => {
                            match frame {
                                Some(close_frame) => {
                                    let code: u16 = close_frame.code.into();

                                    // Try to use methods on close_frame or reason to get a string or bytes
                                    // If reason is a custom UTF-8 type, it likely has methods to get the string content

                                    // Option 1: Try using the Display implementation if it exists
                                    let reason_str = format!("{}", close_frame.reason);

                                    // Option 2: If this is a tungstenite::protocol::CloseFrame
                                    // This might work directly
                                    AxumMessage::Close(Some(axum::extract::ws::CloseFrame {
                                        code,
                                        reason: reason_str.into(),
                                    }))
                                }
                                None => AxumMessage::Close(None),
                            }
                        }
                        TungsteniteMessage::Frame(_) => continue, // 忽略原始帧消息
                    };

                    if let Err(e) = client_sender.send(converted_msg).await {
                        log::error!("Error forwarding message to client: {}", e);
                        break;
                    }
                }
                Err(e) => {
                    log::error!("Error receiving message from server: {}", e);
                    break;
                }
            }
        }
    };

    // 同时处理两个方向的消息转发
    tokio::select! {
        _ = client_to_server => log::info!("Client to server connection closed"),
        _ = server_to_client => log::info!("Server to client connection closed"),
    }

    log::info!("WebSocket forwarding completed");
}
