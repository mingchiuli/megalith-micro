/// Keep compatibility with the OTLP properties already injected into search containers.
pub fn otlp_config() -> crate::OtlpConfig {
    use crate::{OtlpConfig, OtlpSignal};
    use std::env;
    fn signal(name: &str, prefix: &str, property: &str) -> OtlpSignal {
        let key = format!("{prefix}.{property}");
        let endpoint = env::var(format!(
            "OTEL_EXPORTER_OTLP_{}_ENDPOINT",
            name.to_uppercase()
        ))
        .or_else(|_| env::var(&key))
        .or_else(|_| env::var(key.to_uppercase().replace('.', "_")))
        .unwrap_or_else(|_| {
            let base = env::var("OTEL_EXPORTER_OTLP_ENDPOINT")
                .unwrap_or_else(|_| "http://localhost:5080/api/default".into());
            format!("{}/v1/{name}", base.trim_end_matches('/'))
        });
        let authorization = env::var(format!("{prefix}.headers.Authorization"))
            .or_else(|_| {
                env::var(format!(
                    "{}_HEADERS_AUTHORIZATION",
                    prefix.replace('.', "_").to_uppercase()
                ))
            })
            .ok();
        OtlpSignal {
            endpoint,
            authorization,
        }
    }
    OtlpConfig {
        traces: signal(
            "traces",
            "management.opentelemetry.tracing.export.otlp",
            "endpoint",
        ),
        metrics: signal("metrics", "management.otlp.metrics.export", "url"),
        logs: signal(
            "logs",
            "management.opentelemetry.logging.export.otlp",
            "endpoint",
        ),
    }
}
