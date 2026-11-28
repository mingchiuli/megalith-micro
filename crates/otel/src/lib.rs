//! Common OTLP/HTTP exporters and W3C propagation for the Megalith services.
mod propagation;
mod providers;

pub use propagation::{inject_trace_context, set_parent_from_headers, trace_headers};
pub use providers::{OtlpConfig, OtlpSignal, Providers};

mod spring;
pub use spring::otlp_config;
