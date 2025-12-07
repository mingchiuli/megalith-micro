use std::env;

use opentelemetry::KeyValue;
use opentelemetry_otlp::{
    LogExporter, MetricExporter, SpanExporter, WithExportConfig, WithHttpConfig,
};
use opentelemetry_sdk::{
    Resource,
    logs::SdkLoggerProvider,
    metrics::{PeriodicReader, SdkMeterProvider},
    trace::{Sampler, SdkTracerProvider},
};

fn resource() -> Resource {
    Resource::builder()
        .with_service_name("micro-gateway-rs")
        .with_attributes([KeyValue::new("service.version", env!("CARGO_PKG_VERSION"))])
        .build()
}

pub fn init_tracer_provider(http_client: &reqwest::blocking::Client) -> SdkTracerProvider {
    let endpoint = env::var("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT")
        .unwrap_or_else(|_| "http://localhost:8200/v1/traces".to_string());

    let exporter = SpanExporter::builder()
        .with_http()
        .with_http_client(http_client.clone())
        .with_endpoint(&endpoint)
        .build()
        .expect("Failed to create span exporter");

    SdkTracerProvider::builder()
        .with_batch_exporter(exporter)
        .with_sampler(Sampler::TraceIdRatioBased(0.5))
        .with_resource(resource())
        .build()
}

pub fn init_meter_provider(http_client: &reqwest::blocking::Client) -> SdkMeterProvider {
    let endpoint = env::var("OTEL_EXPORTER_OTLP_METRICS_ENDPOINT")
        .unwrap_or_else(|_| "http://localhost:8200/v1/metrics".to_string());

    let exporter = MetricExporter::builder()
        .with_http()
        .with_http_client(http_client.clone())
        .with_endpoint(&endpoint)
        .build()
        .expect("Failed to create metric exporter");

    let reader = PeriodicReader::builder(exporter).build();

    SdkMeterProvider::builder()
        .with_reader(reader)
        .with_resource(resource())
        .build()
}

pub fn init_logger_provider(http_client: &reqwest::blocking::Client) -> SdkLoggerProvider {
    let endpoint = env::var("OTEL_EXPORTER_OTLP_LOGS_ENDPOINT")
        .unwrap_or_else(|_| "http://localhost:8200/v1/logs".to_string());

    let exporter = LogExporter::builder()
        .with_http()
        .with_http_client(http_client.clone())
        .with_endpoint(&endpoint)
        .build()
        .expect("Failed to create log exporter");

    SdkLoggerProvider::builder()
        .with_batch_exporter(exporter)
        .with_resource(resource())
        .build()
}
