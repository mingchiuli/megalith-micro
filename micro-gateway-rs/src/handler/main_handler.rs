use crate::config::config::{self, ConfigKey};

use super::{http_handler, ws_handler};
use axum::extract::FromRequestParts;
use axum::http::header;
use axum::{
    body::Body,
    extract::WebSocketUpgrade,
    http::{Request, Uri},
    response::IntoResponse,
};
use opentelemetry::{KeyValue, global};

pub async fn handle(uri: Uri, mut req: Request<Body>) -> impl IntoResponse {
    // Record metrics
    let meter = global::meter(config::get_static_value(ConfigKey::ServerName));

    let counter = meter
        .u64_counter("http_requests_total")
        .with_description("Total number of HTTP requests")
        .build();
    counter.add(1, &[KeyValue::new("path", uri.path().to_string())]);

    // 检查是否是 WebSocket 请求
    if is_websocket_request(&req) {
        // 分解请求以获取部分
        let (mut parts, body) = req.into_parts();

        // 尝试从请求部分中提取 WebSocketUpgrade
        // 注意: 使用 axum 提供的 FromRequestParts trait
        match WebSocketUpgrade::from_request_parts(&mut parts, &()).await {
            Ok(ws_upgrade) => {
                // 处理 WebSocket 请求
                return match ws_handler::ws_route_handler(ws_upgrade, uri).await {
                    Ok(response) => response.into_response(),
                    Err(err) => {
                        // 根据错误类型返回适当的响应
                        err.into_response()
                    }
                };
            }
            Err(_) => {
                // 如果提取失败，重新组装请求用于 HTTP 处理
                req = Request::from_parts(parts, body);
            }
        }
    }

    // 否则作为普通 HTTP 请求处理
    match http_handler::handle_request(req).await {
        Ok(response) => response.into_response(),
        Err(err) => err.into_response(),
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
