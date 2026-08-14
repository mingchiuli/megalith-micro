use std::{
    sync::{
        Arc,
        atomic::{AtomicU32, AtomicU64, Ordering},
    },
    time::{Duration, SystemTime, UNIX_EPOCH},
};

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
use tokio::sync::{OwnedSemaphorePermit, Semaphore};
use tracing_opentelemetry::OpenTelemetrySpanExt;

use crate::{
    config::{self, ConfigKey},
    exception::ClientError,
};

const CONNECT_TIMEOUT: Duration = Duration::from_millis(300);
const POOL_IDLE_TIMEOUT: Duration = Duration::from_secs(90);
const POOL_MAX_IDLE_PER_HOST: usize = 32;
const CONTROL_RESPONSE_LIMIT: usize = 64 * 1024;
const DEFAULT_AUTH_TIMEOUT: Duration = Duration::from_millis(1200);
const DEFAULT_AUTH_MAX_INFLIGHT: usize = 512;
const AUTH_FAILURE_THRESHOLD: u32 = 10;
const AUTH_CIRCUIT_OPEN: Duration = Duration::from_secs(3);
const AUTH_HALF_OPEN_PROBES: u32 = 3;

pub type HttpClient = Client<HttpConnector, Body>;

#[derive(Clone)]
pub struct GatewayState {
    client: HttpClient,
    auth_timeout: Duration,
    auth_permits: Arc<Semaphore>,
    auth_circuit: Arc<AuthCircuit>,
}

impl GatewayState {
    pub fn new() -> Self {
        Self::with_auth_limits(DEFAULT_AUTH_TIMEOUT, DEFAULT_AUTH_MAX_INFLIGHT)
    }

    pub fn from_config() -> Self {
        let timeout_ms = config::get_config(ConfigKey::AuthTimeoutMs)
            .parse()
            .expect("auth timeout_ms must be a positive integer");
        let max_inflight = config::get_config(ConfigKey::AuthMaxInflight)
            .parse()
            .expect("auth max_inflight must be a positive integer");
        Self::with_auth_limits(Duration::from_millis(timeout_ms), max_inflight)
    }

    fn with_auth_limits(auth_timeout: Duration, max_inflight: usize) -> Self {
        assert!(!auth_timeout.is_zero(), "auth timeout must be positive");
        assert!(max_inflight > 0, "auth max_inflight must be positive");
        let mut connector = HttpConnector::new();
        connector.set_connect_timeout(Some(CONNECT_TIMEOUT));
        connector.set_nodelay(true);

        let client = Client::builder(TokioExecutor::new())
            .pool_idle_timeout(POOL_IDLE_TIMEOUT)
            .pool_max_idle_per_host(POOL_MAX_IDLE_PER_HOST)
            .build(connector);
        Self {
            client,
            auth_timeout,
            auth_permits: Arc::new(Semaphore::new(max_inflight)),
            auth_circuit: Arc::new(AuthCircuit::default()),
        }
    }

    pub fn client(&self) -> &HttpClient {
        &self.client
    }

    pub fn auth_timeout(&self) -> Duration {
        self.auth_timeout
    }

    pub fn acquire_auth(&self) -> Result<AuthCallGuard, ClientError> {
        let half_open = self.auth_circuit.before_call()?;
        let permit = self
            .auth_permits
            .clone()
            .try_acquire_owned()
            .map_err(|_| ClientError::Status(503, "auth service is overloaded".to_string()))?;
        Ok(AuthCallGuard {
            circuit: self.auth_circuit.clone(),
            _permit: permit,
            half_open,
            completed: false,
        })
    }
}

#[derive(Default)]
struct AuthCircuit {
    failures: AtomicU32,
    opened_until_ms: AtomicU64,
    half_open_probes: AtomicU32,
}

impl AuthCircuit {
    fn before_call(&self) -> Result<bool, ClientError> {
        let opened_until = self.opened_until_ms.load(Ordering::Acquire);
        if opened_until == 0 {
            return Ok(false);
        }
        if now_ms() < opened_until {
            return Err(ClientError::Status(
                503,
                "auth service circuit is open".to_string(),
            ));
        }
        let probe = self.half_open_probes.fetch_add(1, Ordering::AcqRel);
        if probe >= AUTH_HALF_OPEN_PROBES {
            self.half_open_probes.fetch_sub(1, Ordering::AcqRel);
            return Err(ClientError::Status(
                503,
                "auth service circuit is half-open".to_string(),
            ));
        }
        Ok(true)
    }

    fn success(&self) {
        self.failures.store(0, Ordering::Release);
        self.opened_until_ms.store(0, Ordering::Release);
        self.half_open_probes.store(0, Ordering::Release);
    }

    fn failure(&self, half_open: bool) {
        if half_open {
            self.failures
                .store(AUTH_FAILURE_THRESHOLD, Ordering::Release);
            self.opened_until_ms.store(
                now_ms() + AUTH_CIRCUIT_OPEN.as_millis() as u64,
                Ordering::Release,
            );
            self.half_open_probes.store(0, Ordering::Release);
            return;
        }
        let failures = self.failures.fetch_add(1, Ordering::AcqRel) + 1;
        if failures >= AUTH_FAILURE_THRESHOLD {
            self.opened_until_ms.store(
                now_ms() + AUTH_CIRCUIT_OPEN.as_millis() as u64,
                Ordering::Release,
            );
            self.half_open_probes.store(0, Ordering::Release);
        }
    }
}

pub struct AuthCallGuard {
    circuit: Arc<AuthCircuit>,
    _permit: OwnedSemaphorePermit,
    half_open: bool,
    completed: bool,
}

impl AuthCallGuard {
    pub fn success(mut self) {
        self.circuit.success();
        self.completed = true;
    }

    pub fn failure(mut self) {
        self.circuit.failure(self.half_open);
        self.completed = true;
    }
}

impl Drop for AuthCallGuard {
    fn drop(&mut self) {
        if self.half_open && !self.completed {
            self.circuit.half_open_probes.fetch_sub(1, Ordering::AcqRel);
        }
    }
}

fn now_ms() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as u64
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

    #[test]
    fn half_open_failure_reopens_the_circuit() {
        let circuit = AuthCircuit::default();
        circuit
            .opened_until_ms
            .store(now_ms().saturating_sub(1), Ordering::Release);

        assert!(circuit.before_call().unwrap());
        circuit.failure(true);

        assert!(matches!(
            circuit.before_call(),
            Err(ClientError::Status(503, _))
        ));
    }
}
