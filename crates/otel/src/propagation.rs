use opentelemetry::global;
use opentelemetry_http::{HeaderExtractor, HeaderInjector};
use reqwest::header::HeaderMap;
use tracing::Span;
use tracing_opentelemetry::OpenTelemetrySpanExt;

pub fn set_parent_from_headers(span: &Span, headers: &HeaderMap) {
    let parent = global::get_text_map_propagator(|p| p.extract(&HeaderExtractor(headers)));
    let _ = span.set_parent(parent);
}

pub fn inject_trace_context(headers: &mut HeaderMap) {
    let context = Span::current().context();
    global::get_text_map_propagator(|p| p.inject_context(&context, &mut HeaderInjector(headers)));
}

pub fn trace_headers() -> HeaderMap {
    let mut headers = HeaderMap::new();
    inject_trace_context(&mut headers);
    headers
}

#[cfg(test)]
mod tests {
    use super::*;
    use opentelemetry::{propagation::TextMapCompositePropagator, trace::TracerProvider as _};
    use opentelemetry_sdk::{
        propagation::{BaggagePropagator, TraceContextPropagator},
        trace::SdkTracerProvider,
    };
    use tracing_subscriber::layer::SubscriberExt;

    #[test]
    fn forwards_the_upstream_trace_state_and_baggage() {
        global::set_text_map_propagator(TextMapCompositePropagator::new(vec![
            Box::new(TraceContextPropagator::new()),
            Box::new(BaggagePropagator::new()),
        ]));
        let provider = SdkTracerProvider::builder().build();
        let subscriber = tracing_subscriber::registry().with(
            tracing_opentelemetry::OpenTelemetryLayer::new(provider.tracer("test")),
        );
        tracing::subscriber::with_default(subscriber, || {
            let mut input = HeaderMap::new();
            let id = "0123456789abcdef0123456789abcdef";
            input.insert(
                "traceparent",
                format!("00-{id}-0123456789abcdef-01").parse().unwrap(),
            );
            input.insert("tracestate", "vendor=sampled".parse().unwrap());
            input.insert("baggage", "locale=zh".parse().unwrap());
            let span = tracing::info_span!("service");
            set_parent_from_headers(&span, &input);
            let out = span.in_scope(trace_headers);
            let parent = out["traceparent"].to_str().unwrap();
            assert_eq!(parent.split('-').nth(1), Some(id));
            assert_ne!(out["traceparent"], input["traceparent"]);
            assert_eq!(out["tracestate"], input["tracestate"]);
            assert_eq!(out["baggage"], input["baggage"]);
        });
    }
}
