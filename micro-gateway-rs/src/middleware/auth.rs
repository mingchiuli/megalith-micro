use axum::http::{HeaderName, HeaderValue, Method, header};
use axum::{extract::Request, middleware::Next, response::Response};
use hyper::Uri;
use serde::Serialize;
use std::collections::HashMap;
use url::Url;

use crate::client;
use crate::config::{self, ConfigKey};
use crate::exception::{AuthError, ClientError, HandlerError, handle_api_error};
use crate::result::ApiResult;
use crate::utils::{self};

use tracing::{Instrument, instrument};

#[derive(Serialize, Debug)]
#[serde(rename_all = "camelCase")]
struct RouteCheckReq {
    method: String,
    route_mapping: String,
}

pub async fn process(req: Request, next: Next) -> Result<Response, HandlerError> {
    // Skip authentication for actuator endpoints
    if req.uri().path() == "/actuator/health" {
        return Ok(next.run(req).await);
    }

    validate_cookie_origin(&req)?;

    let method = req.method();
    let path = req.uri().path();

    // 手动创建动态名称的 span
    let span = tracing::info_span!(
        "request_handler",  // 这个是 tracing 内部名称
        http.method = %method,
        http.uri = %path,
        otel.name = format!("Request: {} {}", method, path)  // 这个才是显示的名称
    );

    async move {
        let (uri, req_body, headers) = extract_request_param(&req)?;
        if !auth(uri, req_body, headers).await? {
            return Err(HandlerError::forbidden("访问被拒绝"));
        }
        Ok(next.run(req).await)
    }
    .instrument(span)
    .await
}

fn validate_cookie_origin(req: &Request) -> Result<(), HandlerError> {
    let origin = request_origin(req);

    if origin.is_none() && !requires_trusted_source(req) && !has_source_header(req) {
        return Ok(());
    }

    let allowed = config::get_config(ConfigKey::AllowedOrigins);
    if origin.is_some_and(|value| is_allowed_origin(&value, &allowed)) {
        return Ok(());
    }

    Err(HandlerError::forbidden("请求来源不受信任"))
}

fn requires_trusted_source(req: &Request) -> bool {
    !matches!(
        req.method(),
        &Method::GET | &Method::HEAD | &Method::OPTIONS
    ) || utils::is_websocket_request(req)
        || utils::has_auth_cookie(req.headers())
}

fn has_source_header(req: &Request) -> bool {
    req.headers().contains_key(header::ORIGIN) || req.headers().contains_key(header::REFERER)
}

fn request_origin(req: &Request) -> Option<String> {
    if let Some(origin) = req.headers().get(header::ORIGIN) {
        return origin.to_str().ok().map(String::from);
    }

    req.headers()
        .get(header::REFERER)
        .and_then(|value| value.to_str().ok())
        .and_then(|value| Url::parse(value).ok())
        .map(|url| url.origin().ascii_serialization())
}

fn is_allowed_origin(origin: &str, allowed_origins: &str) -> bool {
    !origin.is_empty()
        && allowed_origins
            .split(',')
            .map(str::trim)
            .any(|value| value == origin)
}

fn extract_request_param(
    req: &Request,
) -> Result<(Uri, RouteCheckReq, HashMap<HeaderName, HeaderValue>), ClientError> {
    let method = req.method().to_string();
    let path = req.uri().path().to_string();
    let auth_token = utils::extract_token(req);
    let uri = build_auth_uri()?;
    let headers = build_headers(auth_token.as_str());
    let req_body = RouteCheckReq {
        method,
        route_mapping: path,
    };

    Ok((uri, req_body, headers))
}

#[instrument(
    name = "check_auth",
    skip(uri, headers),
    fields(
        auth.route = %req_body.route_mapping,
    )
)]
async fn auth(
    uri: Uri,
    req_body: RouteCheckReq,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<bool, AuthError> {
    let resp: Result<ApiResult<bool>, ClientError> = client::post(uri, req_body, headers)
        .await
        .map_err(handle_api_error);

    map_auth_response(resp)
}

fn map_auth_response(resp: Result<ApiResult<bool>, ClientError>) -> Result<bool, AuthError> {
    match resp {
        Ok(resp) => match resp.code() {
            200 => Ok(resp.into_data()),
            code => Err(AuthError::RequestFailed(format!(
                "鉴权服务返回异常状态码: {code}"
            ))),
        },
        Err(ClientError::Status(code, message)) if code == 401 => {
            Err(AuthError::Unauthorized(message))
        }
        Err(e) => Err(AuthError::RequestFailed(e.to_string())),
    }
}

fn build_auth_uri() -> Result<Uri, ClientError> {
    let mut uri_str = config::get_config(ConfigKey::AuthUrlKey);
    uri_str.push_str("/auth/route/check");

    uri_str
        .parse::<Uri>()
        .map_err(|e| ClientError::Request(e.to_string()))
}

fn build_headers(auth_token: &str) -> HashMap<HeaderName, HeaderValue> {
    HashMap::from([
        (
            hyper::header::AUTHORIZATION,
            HeaderValue::from_str(auth_token).unwrap_or(HeaderValue::from_static("")),
        ),
        (
            hyper::header::CONTENT_TYPE,
            HeaderValue::from_static("application/json"),
        ),
    ])
}

#[cfg(test)]
mod tests {
    use super::{is_allowed_origin, map_auth_response, request_origin, requires_trusted_source};
    use crate::exception::{AuthError, ClientError};
    use crate::result::ApiResult;
    use axum::{body::Body, extract::Request};

    #[test]
    fn matches_a_trimmed_origin_from_the_allowlist() {
        assert!(is_allowed_origin(
            "https://chiu.wiki",
            "http://localhost:1919, https://chiu.wiki"
        ));
    }

    #[test]
    fn rejects_empty_and_partial_origins() {
        assert!(!is_allowed_origin("", "https://chiu.wiki"));
        assert!(!is_allowed_origin(
            "https://chiu.wiki.evil",
            "https://chiu.wiki"
        ));
    }

    #[test]
    fn requires_trusted_source_for_unsafe_methods() {
        for method in ["POST", "PUT", "PATCH", "DELETE"] {
            let req = Request::builder()
                .method(method)
                .uri("/x")
                .body(Body::empty())
                .unwrap();
            assert!(requires_trusted_source(&req));
        }
    }

    #[test]
    fn authenticated_safe_reads_require_a_trusted_source() {
        let authenticated = Request::builder()
            .uri("/x")
            .header("Cookie", "megalith_access_token=access-jwt")
            .body(Body::empty())
            .unwrap();
        let public = Request::builder().uri("/x").body(Body::empty()).unwrap();

        assert!(requires_trusted_source(&authenticated));
        assert!(!requires_trusted_source(&public));
    }

    #[test]
    fn derives_an_exact_origin_from_referer() {
        let request = Request::builder()
            .uri("/x")
            .header("Referer", "https://chiu.wiki/blog?id=1")
            .body(Body::empty())
            .unwrap();

        assert_eq!(
            request_origin(&request).as_deref(),
            Some("https://chiu.wiki")
        );
    }

    #[test]
    fn websocket_gets_require_a_trusted_source() {
        let websocket = Request::builder()
            .uri("/rooms")
            .header("upgrade", "websocket")
            .body(Body::empty())
            .unwrap();
        assert!(requires_trusted_source(&websocket));
    }

    #[test]
    fn preserves_unauthorized_status_from_auth_service() {
        let result = map_auth_response(Err(ClientError::Status(401, "token expired".into())));

        assert!(matches!(
            result,
            Err(AuthError::Unauthorized(message)) if message == "token expired"
        ));
    }

    #[test]
    fn rejects_body_level_unauthorized_as_a_protocol_error() {
        let body: ApiResult<bool> =
            serde_json::from_str(r#"{"code":401,"msg":"unauthorized","data":false}"#).unwrap();

        assert!(matches!(
            map_auth_response(Ok(body)),
            Err(AuthError::RequestFailed(message)) if message.contains("401")
        ));
    }
}
