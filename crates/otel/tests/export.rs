use axum::{
    Router,
    body::Bytes,
    extract::{Path, State},
    http::{HeaderMap, StatusCode},
    routing::post,
};
use megalith_otel::{OtlpConfig, OtlpSignal};
use std::{collections::BTreeSet, net::TcpListener, sync::mpsc, thread};
use tokio::sync::oneshot;

type Export = (String, String, Bytes);

async fn collect(
    Path(signal): Path<String>,
    State(sender): State<mpsc::Sender<Export>>,
    headers: HeaderMap,
    body: Bytes,
) -> StatusCode {
    let auth = headers
        .get("authorization")
        .and_then(|v| v.to_str().ok())
        .unwrap_or_default();
    sender.send((signal, auth.to_owned(), body)).unwrap();
    StatusCode::OK
}

#[test]
fn shutdown_exports_traces_metrics_and_logs_with_their_own_endpoints_and_headers() {
    let listener = TcpListener::bind("127.0.0.1:0").unwrap();
    listener.set_nonblocking(true).unwrap();
    let addr = listener.local_addr().unwrap();
    let (sender, received) = mpsc::channel();
    let (stop, stopped) = oneshot::channel::<()>();
    let collector = thread::spawn(move || {
        tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap()
            .block_on(async {
                let listener = tokio::net::TcpListener::from_std(listener).unwrap();
                let router = Router::new()
                    .route("/v1/{signal}", post(collect))
                    .with_state(sender);
                axum::serve(listener, router)
                    .with_graceful_shutdown(async {
                        let _ = stopped.await;
                    })
                    .await
                    .unwrap();
            });
    });
    let signal = |name: &str| OtlpSignal {
        endpoint: format!("http://{addr}/v1/{name}"),
        authorization: Some(format!("Bearer test-{name}")),
    };
    let config = OtlpConfig {
        traces: signal("traces"),
        metrics: signal("metrics"),
        logs: signal("logs"),
    };
    let providers = config.init("export-test", "1.2.3", "info", 1.0).unwrap();
    tracing::info_span!("flush-test").in_scope(|| tracing::info!("logged inside span"));
    opentelemetry::global::meter("test")
        .u64_counter("test.calls")
        .build()
        .add(1, &[]);
    providers.shutdown();
    stop.send(()).unwrap();
    collector.join().unwrap();

    let mut signals = BTreeSet::new();
    for (signal, auth, body) in received.try_iter() {
        assert_eq!(auth, format!("Bearer test-{signal}"));
        for resource in [b"export-test".as_slice(), b"1.2.3"] {
            assert!(body.windows(resource.len()).any(|field| field == resource));
        }
        signals.insert(signal);
    }
    assert_eq!(
        signals,
        BTreeSet::from(["traces".into(), "metrics".into(), "logs".into()])
    );
}
