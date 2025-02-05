use axum::{
    body::{self, Body, Bytes},
    http::Request,
    middleware::Next,
    response::Response,
};
use hyper::StatusCode;

const MAX_BODY_SIZE: usize = 1024 * 1024; // 1MB

pub async fn process(req: Request<Body>, next: Next) -> Result<Response, StatusCode> {
    // Extract the request body
    if req.uri().path().starts_with("/actuator") {
        return Ok(next.run(req).await);
    }
    
    let (parts, body) = req.into_parts();

    let body_bytes = body::to_bytes(body, MAX_BODY_SIZE)
        .await
        .unwrap_or(Bytes::new());
    let request_body = String::from_utf8_lossy(&body_bytes);
    let uri = parts.uri.to_string();
    let method = parts.method.to_string();
    log::info!("Url Method Request: {} {} {:?}", uri, method, request_body);

    // Reconstruct the request with the extracted body
    let req = Request::from_parts(parts, Body::from(body_bytes.clone()));

    // Call the next middleware or handler
    let response = next.run(req).await;

    // Extract the response body
    let (parts, body) = response.into_parts();
    let body_bytes = body::to_bytes(body, MAX_BODY_SIZE)
        .await
        .unwrap_or(Bytes::new());
    let response_body = String::from_utf8_lossy(&body_bytes);

    log::info!("Response: {} {} {:?}", uri, method, response_body);

    // Reconstruct the response with the extracted body
    Ok(Response::from_parts(parts, Body::from(body_bytes)))
}
