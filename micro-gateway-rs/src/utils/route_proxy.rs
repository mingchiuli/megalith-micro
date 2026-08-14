use axum::{
    body::Body,
    http::{HeaderName, HeaderValue},
    response::Response,
};
use base64::{Engine as _, engine::general_purpose::URL_SAFE_NO_PAD};
use hyper::{HeaderMap, Method, StatusCode, Uri, body::Incoming, header};
use opentelemetry::global;
use opentelemetry_http::HeaderInjector;
use tracing_opentelemetry::OpenTelemetrySpanExt;

use crate::{
    client::{self, AuthPrincipal, AuthRouteReq, AuthRouteResp, GatewayState},
    config::{self, ConfigKey},
    constant::UNKNOWN,
    exception::{ClientError, handle_api_error},
    result::ApiResult,
};

use super::request_metadata::{REFRESH_TOKEN_COOKIE, cookie_value, get_ip_from_headers};

const REFRESH_TOKEN_PATH: &str = "/token/refresh";
pub const PRINCIPAL_HEADER: &str = "x-megalith-principal";

pub fn get_auth_url() -> Result<Uri, ClientError> {
    let mut auth_url = config::get_config(ConfigKey::AuthUrlKey);
    auth_url.push_str("/auth/route");
    auth_url
        .parse::<Uri>()
        .map_err(|error| ClientError::Request(format!("invalid auth URL: {error}")))
}

#[tracing::instrument(
    name = "find_target_address",
    skip(state, req_body, token),
    fields(http.url = %auth_url)
)]
pub async fn find_route(
    state: &GatewayState,
    auth_url: Uri,
    req_body: AuthRouteReq,
    token: &str,
) -> Result<AuthRouteResp, ClientError> {
    let guard = state.acquire_auth()?;
    let mut headers = HeaderMap::new();
    headers.insert(
        header::AUTHORIZATION,
        HeaderValue::from_str(token).unwrap_or(HeaderValue::from_static("")),
    );
    headers.insert(
        header::CONTENT_TYPE,
        HeaderValue::from_static("application/json"),
    );

    let response = tokio::time::timeout(
        state.auth_timeout(),
        client::post(state.client(), auth_url, req_body, headers),
    )
    .await;
    let resp: ApiResult<AuthRouteResp> = match response {
        Err(_) => {
            guard.failure();
            return Err(ClientError::Status(
                StatusCode::SERVICE_UNAVAILABLE.as_u16(),
                "auth service timed out".to_string(),
            ));
        }
        Ok(Err(error)) => {
            let error = handle_api_error(error);
            if matches!(&error, ClientError::Status(code, _) if *code >= 500) {
                guard.failure();
                return Err(ClientError::Status(
                    StatusCode::SERVICE_UNAVAILABLE.as_u16(),
                    error.to_string(),
                ));
            }
            guard.success();
            return Err(error);
        }
        Ok(Ok(resp)) => resp,
    };
    if resp.code() != i32::from(StatusCode::OK.as_u16()) {
        let code = u16::try_from(resp.code()).unwrap_or(StatusCode::BAD_GATEWAY.as_u16());
        if code >= 500 {
            guard.failure();
            return Err(ClientError::Status(
                StatusCode::SERVICE_UNAVAILABLE.as_u16(),
                format!("auth service returned body code {}", resp.code()),
            ));
        }
        guard.success();
        return Err(ClientError::Status(
            code,
            format!("auth service returned body code {}", resp.code()),
        ));
    }
    guard.success();
    Ok(resp.into_data())
}

pub fn prepare_route_request(method: &Method, headers: &HeaderMap, uri: &Uri) -> AuthRouteReq {
    let ip_addr = get_ip_from_headers(headers).unwrap_or_else(|| UNKNOWN.to_string());
    AuthRouteReq::new(method, uri.path().to_string(), ip_addr)
}

pub fn parse_url(route: &AuthRouteResp, uri: &Uri, protocol: &str) -> Result<Uri, ClientError> {
    let path_and_query = uri
        .path_and_query()
        .ok_or_else(|| ClientError::Request("Invalid URI".to_string()))?;
    format!(
        "{}://{}:{}{}",
        protocol,
        route.service_host(),
        route.service_port(),
        path_and_query
    )
    .parse::<Uri>()
    .map_err(|error| ClientError::Request(format!("parse: {error}")))
}

pub fn prepare_headers(
    request_headers: &HeaderMap,
    principal: &AuthPrincipal,
    request_path: &str,
) -> Result<HeaderMap, ClientError> {
    let mut headers = request_headers.clone();
    remove_hop_by_hop_headers(&mut headers);
    for name in [header::HOST, header::COOKIE, header::AUTHORIZATION] {
        headers.remove(name);
    }
    headers.remove(PRINCIPAL_HEADER);

    if request_path == REFRESH_TOKEN_PATH
        && let Some(refresh_token) = cookie_value(request_headers, REFRESH_TOKEN_COOKIE)
    {
        headers.insert(
            header::COOKIE,
            HeaderValue::from_str(&format!("{REFRESH_TOKEN_COOKIE}={refresh_token}")).map_err(
                |error| ClientError::Request(format!("invalid refresh cookie: {error}")),
            )?,
        );
    }

    headers.insert(
        HeaderName::from_static(PRINCIPAL_HEADER),
        principal_header_value(principal)?,
    );
    Ok(headers)
}

pub fn principal_header_value(principal: &AuthPrincipal) -> Result<HeaderValue, ClientError> {
    let json = serde_json::to_vec(principal)
        .map_err(|error| ClientError::Serialization(error.to_string()))?;
    HeaderValue::from_str(&URL_SAFE_NO_PAD.encode(json))
        .map_err(|error| ClientError::Request(error.to_string()))
}

pub fn prepare_response(resp: Response<Incoming>) -> Response<Body> {
    let (mut parts, body) = resp.into_parts();
    remove_hop_by_hop_headers(&mut parts.headers);
    Response::from_parts(parts, Body::new(body))
}

fn remove_hop_by_hop_headers(headers: &mut HeaderMap) {
    let nominated: Vec<HeaderName> = headers
        .get_all(header::CONNECTION)
        .iter()
        .filter_map(|value| value.to_str().ok())
        .flat_map(|value| value.split(','))
        .filter_map(|name| HeaderName::from_bytes(name.trim().as_bytes()).ok())
        .collect();
    for name in nominated {
        headers.remove(name);
    }
    for name in [
        header::CONNECTION,
        HeaderName::from_static("keep-alive"),
        header::PROXY_AUTHENTICATE,
        header::PROXY_AUTHORIZATION,
        header::TE,
        header::TRAILER,
        header::TRANSFER_ENCODING,
        header::UPGRADE,
    ] {
        headers.remove(name);
    }
}

pub fn inject_trace_context(headers: &mut HeaderMap) {
    global::get_text_map_propagator(|propagator| {
        let current_context = tracing::Span::current().context();
        propagator.inject_context(&current_context, &mut HeaderInjector(headers));
    });
}

#[cfg(test)]
mod tests {
    use super::*;
    use base64::engine::general_purpose::URL_SAFE_NO_PAD;

    fn route() -> AuthRouteResp {
        serde_json::from_str(
            r#"{"serviceHost":"example.com","servicePort":8080,"principal":{"userId":42,"roles":["user"]}}"#,
        )
        .unwrap()
    }

    #[test]
    fn prepares_route_metadata() {
        let mut headers = HeaderMap::new();
        headers.insert("CF-Connecting-IP", "8.8.8.8".parse().unwrap());
        let uri = "/api/v1/foo".parse::<Uri>().unwrap();
        let req = prepare_route_request(&Method::GET, &headers, &uri);
        let json = serde_json::to_string(&req).unwrap();
        assert!(json.contains("8.8.8.8"));
        assert!(json.contains("/api/v1/foo"));
    }

    #[test]
    fn refresh_forwards_only_refresh_cookie_and_trusted_principal() {
        let mut headers = HeaderMap::new();
        headers.insert(
            header::COOKIE,
            HeaderValue::from_static(
                "other=1; megalith_access_token=access; megalith_refresh_token=refresh",
            ),
        );
        headers.insert(
            header::AUTHORIZATION,
            HeaderValue::from_static("client-token"),
        );
        headers.insert(PRINCIPAL_HEADER, HeaderValue::from_static("forged"));

        let result = prepare_headers(&headers, route().principal(), REFRESH_TOKEN_PATH).unwrap();
        assert_eq!(
            result.get(header::COOKIE).unwrap(),
            "megalith_refresh_token=refresh"
        );
        assert!(!result.contains_key(header::AUTHORIZATION));
        let encoded = result.get(PRINCIPAL_HEADER).unwrap().to_str().unwrap();
        let json = String::from_utf8(URL_SAFE_NO_PAD.decode(encoded).unwrap()).unwrap();
        assert_eq!(
            json,
            r#"{"userId":42,"roles":["user"],"dataPermissions":[]}"#
        );
    }

    #[test]
    fn strips_hop_by_hop_and_client_credentials() {
        let mut headers = HeaderMap::new();
        headers.insert(header::CONNECTION, HeaderValue::from_static("x-remove"));
        headers.insert("x-remove", HeaderValue::from_static("value"));
        headers.insert(header::COOKIE, HeaderValue::from_static("session=x"));
        headers.insert(header::AUTHORIZATION, HeaderValue::from_static("Bearer x"));

        let result = prepare_headers(&headers, route().principal(), "/blog/list").unwrap();
        assert!(!result.contains_key(header::CONNECTION));
        assert!(!result.contains_key("x-remove"));
        assert!(!result.contains_key(header::COOKIE));
        assert!(!result.contains_key(header::AUTHORIZATION));
    }

    #[test]
    fn parse_url_preserves_query() {
        let uri = "/path?x=1".parse::<Uri>().unwrap();
        assert_eq!(
            parse_url(&route(), &uri, "http").unwrap().to_string(),
            "http://example.com:8080/path?x=1"
        );
    }
}
