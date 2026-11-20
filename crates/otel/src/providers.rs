use anyhow::Result;
use opentelemetry::{
    KeyValue, global, propagation::TextMapCompositePropagator, trace::TracerProvider as _,
};
use opentelemetry_appender_tracing::layer::OpenTelemetryTracingBridge;
use opentelemetry_otlp::{
    LogExporter, MetricExporter, SpanExporter, WithExportConfig, WithHttpConfig,
};
use opentelemetry_sdk::{
    Resource,
    logs::SdkLoggerProvider,
    metrics::SdkMeterProvider,
    propagation::{BaggagePropagator, TraceContextPropagator},
    trace::{Sampler, SdkTracerProvider},
};
use serde::Deserialize;
use std::{collections::HashMap, env};
use tracing_opentelemetry::OpenTelemetryLayer;
use tracing_subscriber::{EnvFilter, layer::SubscriberExt, util::SubscriberInitExt};

#[derive(Clone, Deserialize)]
pub struct OtlpSignal {
    pub endpoint: String,
    #[serde(skip)]
    pub authorization: Option<String>,
}

impl OtlpSignal {
    fn headers(&self) -> HashMap<String, String> {
        self.authorization
            .iter()
            .map(|value| ("Authorization".into(), value.clone()))
            .collect()
    }
}

#[derive(Clone, Deserialize)]
pub struct OtlpConfig {
    pub traces: OtlpSignal,
    pub metrics: OtlpSignal,
    pub logs: OtlpSignal,
}

pub struct Providers {
    traces: SdkTracerProvider,
    metrics: SdkMeterProvider,
    logs: SdkLoggerProvider,
}

impl OtlpConfig {
    /// Initialize before entering Tokio. OTLP batch workers share a blocking HTTP client.
    pub fn init(
        &self,
        service_name: &str,
        version: &'static str,
        filter: &str,
        default_sample_ratio: f64,
    ) -> Result<Providers> {
        let client = reqwest::blocking::Client::new();
        let resource = Resource::builder()
            .with_service_name(service_name.to_owned())
            .with_attribute(KeyValue::new("service.version", version))
            .build();

        let trace = SpanExporter::builder()
            .with_http()
            .with_http_client(client.clone())
            .with_endpoint(&self.traces.endpoint)
            .with_headers(self.traces.headers())
            .build()?;
        let metric = MetricExporter::builder()
            .with_http()
            .with_http_client(client.clone())
            .with_endpoint(&self.metrics.endpoint)
            .with_headers(self.metrics.headers())
            .build()?;
        let log = LogExporter::builder()
            .with_http()
            .with_http_client(client)
            .with_endpoint(&self.logs.endpoint)
            .with_headers(self.logs.headers())
            .build()?;

        let mut builder = SdkTracerProvider::builder()
            .with_resource(resource.clone())
            .with_batch_exporter(trace);
        if env::var_os("OTEL_TRACES_SAMPLER").is_none() {
            builder = builder.with_sampler(Sampler::ParentBased(Box::new(
                Sampler::TraceIdRatioBased(default_sample_ratio),
            )));
        }
        let traces = builder.build();
        let metrics = SdkMeterProvider::builder()
            .with_resource(resource.clone())
            .with_periodic_exporter(metric)
            .build();
        let logs = SdkLoggerProvider::builder()
            .with_resource(resource)
            .with_batch_exporter(log)
            .build();

        global::set_tracer_provider(traces.clone());
        global::set_meter_provider(metrics.clone());
        global::set_text_map_propagator(TextMapCompositePropagator::new(vec![
            Box::new(TraceContextPropagator::new()),
            Box::new(BaggagePropagator::new()),
        ]));
        tracing_subscriber::registry()
            .with(EnvFilter::new(filter))
            .with(tracing_subscriber::fmt::layer())
            .with(OpenTelemetryLayer::new(
                traces.tracer(service_name.to_owned()),
            ))
            .with(OpenTelemetryTracingBridge::new(&logs))
            .try_init()?;
        Ok(Providers {
            traces,
            metrics,
            logs,
        })
    }
}

impl Providers {
    /// Flush all three signals after the application runtime finishes.
    pub fn shutdown(self) {
        let _ = self.traces.shutdown();
        let _ = self.metrics.shutdown();
        let _ = self.logs.shutdown();
    }
}
