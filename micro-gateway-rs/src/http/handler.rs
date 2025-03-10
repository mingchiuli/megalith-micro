use super::{http_handler, ws_handler};
use axum::http::header;
use axum::{
    body::Body,
    extract::WebSocketUpgrade,
    http::{Request, Uri},
    response::IntoResponse,
};
use hyper::StatusCode;

pub async fn unified_handler(
    ws: WebSocketUpgrade,
    uri: Uri,
    req: Request<Body>,
) -> impl IntoResponse {
    // 检查是否是 WebSocket 请求，并且路径是 /edit/ws
    if is_websocket_request(&req) {
        // 处理 WebSocket 请求并直接返回
        return match ws_handler::ws_route_handler(ws, uri).await {
            Ok(response) => response.into_response(),
            Err(err) => {
                // 根据错误类型返回适当的响应
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    format!("WebSocket error: {}", err),
                )
                    .into_response()
            }
        };
    }

    // 否则作为普通 HTTP 请求处理
    match http_handler::handle_request(req).await {
        Ok(response) => response.into_response(),
        Err(status) => (status, "Error processing request").into_response(),
    }
}

fn is_websocket_request(req: &Request<Body>) -> bool {
    // 根据 RFC 6455，检查 WebSocket 必要的请求头
    // https://datatracker.ietf.org/doc/html/rfc6455#section-4.1

    // 检查 Connection 头包含 "upgrade"
    let connection_upgrade = req
        .headers()
        .get(header::CONNECTION)
        .and_then(|v| v.to_str().ok())
        .map(|s| {
            s.split(',')
                .map(|p| p.trim().to_lowercase())
                .any(|p| p == "upgrade")
        })
        .unwrap_or(false);

    // 检查 Upgrade 头值为 "websocket"
    let upgrade_websocket = req
        .headers()
        .get(header::UPGRADE)
        .and_then(|v| v.to_str().ok())
        .map(|s| s.to_lowercase() == "websocket")
        .unwrap_or(false);

    // 检查 Sec-WebSocket-Key 头（WebSocket 握手需要）
    let has_ws_key = req.headers().contains_key("sec-websocket-key");

    // 检查 Sec-WebSocket-Version 头（WebSocket 握手需要）
    let has_ws_version = req.headers().contains_key("sec-websocket-version");

    // 所有条件都满足才是合法的 WebSocket 请求
    connection_upgrade && upgrade_websocket && has_ws_key && has_ws_version
}
