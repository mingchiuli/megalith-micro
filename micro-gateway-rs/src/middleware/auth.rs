use axum::{
    extract::{Request, State},
    middleware::Next,
    response::Response,
};
use tracing::Instrument;

use crate::client::GatewayState;
use crate::exception::HandlerError;
use crate::proxy::{extract_token, prepare_route_request, validate_cookie_origin};

pub async fn process(
    State(state): State<GatewayState>,
    mut request: Request,
    next: Next,
) -> Result<Response, HandlerError> {
    if request.uri().path() == "/actuator/health" {
        return Ok(next.run(request).await);
    }

    validate_cookie_origin(&request)?;

    let method = request.method();
    let path = request.uri().path();
    let span = tracing::info_span!(
        "request_handler",
        http.method = %method,
        http.uri = %path,
        otel.name = format!("Request: {method} {path}")
    );

    async move {
        let token = if request.uri().path() == "/token/refresh" {
            String::new()
        } else {
            extract_token(&request)
        };
        let route_request =
            prepare_route_request(request.method(), request.headers(), request.uri());
        let route = state.auth().authorize(route_request, &token).await?;
        request.extensions_mut().insert(route);
        Ok(next.run(request).await)
    }
    .instrument(span)
    .await
}
