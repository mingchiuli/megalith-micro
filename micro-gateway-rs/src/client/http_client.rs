use std::time::Duration;

use axum::{
    BoxError,
    body::Body,
    extract::Request,
    http::{HeaderMap, HeaderValue, request::Builder},
};
use http_body_util::{BodyExt, Limited};
use hyper::{Method, Response, Uri, body::Incoming, header};
use hyper_util::{
    client::legacy::{Client, connect::HttpConnector},
    rt::TokioExecutor,
};
use opentelemetry::global;
use opentelemetry_http::HeaderInjector;
use serde::{Deserialize, Serialize, de::DeserializeOwned};
use tracing_opentelemetry::OpenTelemetrySpanExt;

use crate::exception::ClientError;

const CONNECT_TIMEOUT: Duration = Duration::from_secs(3);
const POOL_IDLE_TIMEOUT: Duration = Duration::from_secs(90);
const POOL_MAX_IDLE_PER_HOST: usize = 32;
const CONTROL_RESPONSE_LIMIT: usize = 1024 * 1024;

pub type HttpClient = Client<HttpConnector, Body>;

#[derive(Clone)]
pub struct GatewayState {
    client: HttpClient,
}

impl GatewayState {
    pub fn new() -> Self {
        let mut connector = HttpConnector::new();
        connector.set_connect_timeout(Some(CONNECT_TIMEOUT));
        connector.set_nodelay(true);

        let client = Client::builder(TokioExecutor::new())
            .pool_idle_timeout(POOL_IDLE_TIMEOUT)
            .pool_max_idle_per_host(POOL_MAX_IDLE_PER_HOST)
            .build(connector);
        Self { client }
    }

    pub fn client(&self) -> &HttpClient {
        &self.client
    }
}

impl Default for GatewayState {
    fn default() -> Self {
        Self::new()
    }
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthRouteReq {
    method: String,
    route_mapping: String,
    ip_addr: String,
}

impl AuthRouteReq {
    pub fn new(method: &Method, route_mapping: String, ip_addr: String) -> Self {
        Self {
            method: method.to_string(),
            route_mapping,
            ip_addr,
        }
    }
}

#[derive(Clone, Deserialize, Serialize, Debug, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct AuthPrincipal {
    user_id: u64,
    roles: Vec<String>,
    #[serde(default)]
    data_permissions: Vec<String>,
}

#[derive(Clone, Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct AuthRouteResp {
    service_host: String,
    service_port: u32,
    principal: AuthPrincipal,
}

impl AuthRouteResp {
    pub fn service_host(&self) -> &str {
        &self.service_host
    }

    pub fn service_port(&self) -> &u32 {
        &self.service_port
    }

    pub fn principal(&self) -> &AuthPrincipal {
        &self.principal
    }
}

async fn parse_req(
    url: Uri,
    method: Method,
    mut headers: HeaderMap,
) -> Result<Builder, ClientError> {
    inject_trace_context_hashmap(&mut headers);
    let host = parse_host(&url)?;
    let mut req = Request::builder()
        .method(method)
        .uri(url)
        .header(header::HOST, host);
    if let Some(request_headers) = req.headers_mut() {
        request_headers.extend(headers);
    }
    Ok(req)
}

fn inject_trace_context_hashmap(headers: &mut HeaderMap) {
    global::get_text_map_propagator(|propagator| {
        let current_context = tracing::Span::current().context();
        propagator.inject_context(&current_context, &mut HeaderInjector(headers));
    });
}

pub async fn post<T: DeserializeOwned>(
    client: &HttpClient,
    url: Uri,
    body: impl Serialize,
    headers: HeaderMap,
) -> Result<T, BoxError> {
    let json =
        serde_json::to_vec(&body).map_err(|error| ClientError::Serialization(error.to_string()))?;
    let response = request_raw(client, Method::POST, url, Body::from(json), headers).await?;
    parse_response(response).await
}

fn parse_host(url: &Uri) -> Result<HeaderValue, ClientError> {
    let host = url
        .authority()
        .ok_or(ClientError::Request("No authority found".to_string()))?
        .as_str();
    HeaderValue::from_str(host).map_err(|_| ClientError::Request("Invalid host".to_string()))
}

async fn parse_response<T>(response: Response<Incoming>) -> Result<T, BoxError>
where
    T: DeserializeOwned,
{
    let status = response.status();
    let body = Limited::new(response.into_body(), CONTROL_RESPONSE_LIMIT)
        .collect()
        .await
        .map_err(|error| ClientError::Response(error.to_string()))?
        .to_bytes();
    if !status.is_success() {
        return Err(Box::new(ClientError::Status(
            status.as_u16(),
            String::from_utf8_lossy(&body).to_string(),
        )));
    }
    serde_json::from_slice::<T>(&body)
        .map_err(|error| Box::new(ClientError::Deserialize(error.to_string())) as BoxError)
}

pub async fn request_raw(
    client: &HttpClient,
    method: Method,
    url: Uri,
    body: Body,
    headers: HeaderMap,
) -> Result<Response<Incoming>, BoxError> {
    let req = parse_req(url, method, headers)
        .await?
        .body(body)
        .map_err(|error| ClientError::Request(error.to_string()))?;
    client
        .request(req)
        .await
        .map_err(|error| Box::new(ClientError::Network(error.to_string())) as BoxError)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn principal_serializes_as_the_java_contract() {
        let principal = AuthPrincipal {
            user_id: 42,
            roles: vec!["user".to_string()],
            data_permissions: vec!["BLOG_VIEW_ALL".to_string()],
        };
        assert_eq!(
            serde_json::to_string(&principal).unwrap(),
            r#"{"userId":42,"roles":["user"],"dataPermissions":["BLOG_VIEW_ALL"]}"#
        );
    }
}
