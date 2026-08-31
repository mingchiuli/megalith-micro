use std::sync::Arc;
use std::sync::atomic::{AtomicU32, AtomicU64, Ordering};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

use axum::http::{HeaderMap, HeaderValue, StatusCode, Uri, header};
use tokio::sync::{OwnedSemaphorePermit, Semaphore};

use super::http_client::{HttpClient, post};
use crate::config::{self, ConfigKey};
use crate::exception::{ClientError, handle_api_error};
use crate::proxy::{AuthRouteRequest, AuthorizedRoute};
use crate::result::ApiResult;

const DEFAULT_AUTH_TIMEOUT: Duration = Duration::from_millis(1200);
const DEFAULT_AUTH_MAX_INFLIGHT: usize = 512;
const AUTH_FAILURE_THRESHOLD: u32 = 10;
const AUTH_CIRCUIT_OPEN: Duration = Duration::from_secs(3);
const AUTH_HALF_OPEN_PROBES: u32 = 3;

#[derive(Clone)]
pub(crate) struct AuthClient {
    client: HttpClient,
    auth_url: Uri,
    timeout: Duration,
    permits: Arc<Semaphore>,
    circuit: Arc<AuthCircuit>,
}

impl AuthClient {
    pub(crate) fn new(client: HttpClient) -> Self {
        Self::with_settings(
            client,
            "http://127.0.0.1/auth/route".parse().expect("valid URL"),
            DEFAULT_AUTH_TIMEOUT,
            DEFAULT_AUTH_MAX_INFLIGHT,
        )
    }

    pub(crate) fn from_config(client: HttpClient) -> Self {
        let timeout_ms = config::get_config(ConfigKey::AuthTimeoutMs)
            .parse()
            .expect("auth timeout_ms must be a positive integer");
        let max_inflight = config::get_config(ConfigKey::AuthMaxInflight)
            .parse()
            .expect("auth max_inflight must be a positive integer");
        let mut auth_url = config::get_config(ConfigKey::AuthUrlKey);
        auth_url.push_str("/auth/route");
        Self::with_settings(
            client,
            auth_url.parse().expect("invalid auth URL"),
            Duration::from_millis(timeout_ms),
            max_inflight,
        )
    }

    fn with_settings(
        client: HttpClient,
        auth_url: Uri,
        timeout: Duration,
        max_inflight: usize,
    ) -> Self {
        assert!(!timeout.is_zero(), "auth timeout must be positive");
        assert!(max_inflight > 0, "auth max_inflight must be positive");
        Self {
            client,
            auth_url,
            timeout,
            permits: Arc::new(Semaphore::new(max_inflight)),
            circuit: Arc::new(AuthCircuit::default()),
        }
    }

    #[tracing::instrument(
        name = "find_target_address",
        skip(self, request, token),
        fields(http.url = %self.auth_url)
    )]
    pub(crate) async fn authorize(
        &self,
        request: AuthRouteRequest,
        token: &str,
    ) -> Result<AuthorizedRoute, ClientError> {
        let guard = self.acquire()?;
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
            self.timeout,
            post(&self.client, self.auth_url.clone(), request, headers),
        )
        .await;
        let response: ApiResult<AuthorizedRoute> = match response {
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
            Ok(Ok(response)) => response,
        };
        if response.code() != i32::from(StatusCode::OK.as_u16()) {
            let code = u16::try_from(response.code()).unwrap_or(StatusCode::BAD_GATEWAY.as_u16());
            if code >= 500 {
                guard.failure();
                return Err(ClientError::Status(
                    StatusCode::SERVICE_UNAVAILABLE.as_u16(),
                    format!("auth service returned body code {}", response.code()),
                ));
            }
            guard.success();
            return Err(ClientError::Status(
                code,
                format!("auth service returned body code {}", response.code()),
            ));
        }
        guard.success();
        Ok(response.into_data())
    }

    fn acquire(&self) -> Result<AuthCallGuard, ClientError> {
        let half_open = self.circuit.before_call()?;
        let permit = self
            .permits
            .clone()
            .try_acquire_owned()
            .map_err(|_| ClientError::Status(503, "auth service is overloaded".to_string()))?;
        Ok(AuthCallGuard {
            circuit: Arc::clone(&self.circuit),
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

struct AuthCallGuard {
    circuit: Arc<AuthCircuit>,
    _permit: OwnedSemaphorePermit,
    half_open: bool,
    completed: bool,
}

impl AuthCallGuard {
    fn success(mut self) {
        self.circuit.success();
        self.completed = true;
    }

    fn failure(mut self) {
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

#[cfg(test)]
mod tests {
    use super::*;
    use crate::client::http_client::create_http_client;
    use axum::{Json, Router, routing::post as route_post};
    use hyper::Method;

    fn request() -> AuthRouteRequest {
        AuthRouteRequest::new(
            &Method::GET,
            "/blog/list".to_string(),
            "127.0.0.1".to_string(),
        )
    }

    async fn spawn(app: Router) -> (Uri, tokio::task::JoinHandle<()>) {
        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let address = listener.local_addr().unwrap();
        let server = tokio::spawn(async move {
            axum::serve(listener, app).await.unwrap();
        });
        (
            format!("http://{address}/auth/route").parse().unwrap(),
            server,
        )
    }

    #[tokio::test]
    async fn returns_an_authorized_route_from_the_control_plane() {
        let app = Router::new().route(
            "/auth/route",
            route_post(|| async {
                Json(serde_json::json!({
                    "code": 200,
                    "msg": "ok",
                    "data": {
                        "serviceHost": "blog",
                        "servicePort": 8083,
                        "principal": {"userId": 42, "roles": ["user"]}
                    }
                }))
            }),
        );
        let (url, server) = spawn(app).await;
        let client =
            AuthClient::with_settings(create_http_client(), url, Duration::from_secs(1), 1);

        let route = client.authorize(request(), "Bearer token").await.unwrap();

        assert_eq!(route.service_host(), "blog");
        assert_eq!(route.service_port(), 8083);
        server.abort();
    }

    #[tokio::test]
    async fn reports_control_plane_timeouts_as_service_unavailable() {
        let app = Router::new().route(
            "/auth/route",
            route_post(|| async {
                tokio::time::sleep(Duration::from_millis(100)).await;
                Json(serde_json::json!({
                    "code": 200,
                    "msg": "ok",
                    "data": {
                        "serviceHost": "blog",
                        "servicePort": 8083,
                        "principal": {"userId": 42, "roles": ["user"]}
                    }
                }))
            }),
        );
        let (url, server) = spawn(app).await;
        let client =
            AuthClient::with_settings(create_http_client(), url, Duration::from_millis(10), 1);

        assert!(matches!(
            client.authorize(request(), "Bearer token").await,
            Err(ClientError::Status(503, message)) if message.contains("timed out")
        ));
        server.abort();
    }

    #[tokio::test]
    async fn preserves_non_retryable_auth_statuses_without_opening_the_circuit() {
        let app = Router::new().route(
            "/auth/route",
            route_post(|| async {
                Json(serde_json::json!({
                    "code": 401,
                    "msg": "unauthorized",
                    "data": {
                        "serviceHost": "blog",
                        "servicePort": 8083,
                        "principal": {"userId": 0, "roles": []}
                    }
                }))
            }),
        );
        let (url, server) = spawn(app).await;
        let client =
            AuthClient::with_settings(create_http_client(), url, Duration::from_secs(1), 1);

        assert!(matches!(
            client.authorize(request(), "Bearer invalid").await,
            Err(ClientError::Status(401, _))
        ));
        assert_eq!(client.circuit.failures.load(Ordering::Acquire), 0);
        server.abort();
    }

    #[test]
    fn rejects_calls_above_the_auth_concurrency_limit() {
        let client = AuthClient::with_settings(
            create_http_client(),
            "http://127.0.0.1/auth/route".parse().unwrap(),
            Duration::from_secs(1),
            1,
        );
        let _active = client.acquire().unwrap();

        assert!(matches!(
            client.acquire(),
            Err(ClientError::Status(503, message)) if message.contains("overloaded")
        ));
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

    #[test]
    fn successful_half_open_probe_closes_the_circuit() {
        let circuit = AuthCircuit::default();
        circuit
            .failures
            .store(AUTH_FAILURE_THRESHOLD, Ordering::Release);
        circuit
            .opened_until_ms
            .store(now_ms().saturating_sub(1), Ordering::Release);

        assert!(circuit.before_call().unwrap());
        circuit.success();

        assert!(!circuit.before_call().unwrap());
        assert_eq!(circuit.failures.load(Ordering::Acquire), 0);
    }
}
