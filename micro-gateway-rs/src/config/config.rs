use std::env;

pub enum ConfigKey {
    ServerName,
    ServerPort,
    OtelExporterOtlpTracesEndpoint,
    OtelExporterOtlpMetricsEndpoint,
    OtelExporterOtlpLogsEndpoint,
    AuthUrlKey,
    RustLog
}

impl ConfigKey {
    /// 获取环境变量名
    fn env_name(&self) -> &str {
        match self {
            ConfigKey::ServerName => "SERVER_NAME",
            ConfigKey::ServerPort => "SERVER_PORT",
            ConfigKey::OtelExporterOtlpTracesEndpoint => "OTEL_EXPORTER_OTLP_TRACES_ENDPOINT",
            ConfigKey::OtelExporterOtlpMetricsEndpoint => "OTEL_EXPORTER_OTLP_METRICS_ENDPOINT",
            ConfigKey::OtelExporterOtlpLogsEndpoint => "OTEL_EXPORTER_OTLP_LOGS_ENDPOINT",
            ConfigKey::AuthUrlKey => "AUTH_URL_KEY",
            ConfigKey::RustLog => "RUST_LOG",
        }
    }

    /// 获取默认值
    fn default_value(&self) -> &str {
        match self {
            ConfigKey::ServerName => "micro-gateway-rs",
            ConfigKey::ServerPort => "8088",
            ConfigKey::OtelExporterOtlpTracesEndpoint => "http://localhost:8200/v1/traces",
            ConfigKey::OtelExporterOtlpMetricsEndpoint => "http://localhost:8200/v1/metrics",
            ConfigKey::OtelExporterOtlpLogsEndpoint => "http://localhost:8200/v1/logs",
            ConfigKey::AuthUrlKey => "http://127.0.0.1:8081/inner",
            ConfigKey::RustLog => "info",
        }
    }
}

/// 从环境变量获取配置，若不存在则使用枚举的默认值
/// # 参数
/// * `key` - 配置项枚举
/// # 返回
/// 配置值的字符串
pub fn get_config(key: ConfigKey) -> String {
    env::var(key.env_name()).unwrap_or_else(|_| key.default_value().to_string())
}

pub fn get_static_value(key: ConfigKey) -> &'static str {
    Box::leak(get_config(key).into_boxed_str())
}
