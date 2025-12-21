use config::{Config, ConfigError, Environment};
use serde::Deserialize;
use std::sync::OnceLock;

#[derive(Deserialize)]
struct AppConfig {
    server: ServerConfig,
    otel: OtelConfig,
    log: LogConfig,
}

#[derive(Deserialize)]
struct ServerConfig {
    name: String,
    port: u16,
}

#[derive(Deserialize)]
struct OtelConfig {
    exporter: ExporterConfig,
}

#[derive(Deserialize)]
struct ExporterConfig {
    otlp: OtlpConfig,
}

#[derive(Deserialize)]
struct OtlpConfig {
    traces: TracesConfig,
    metrics: MetricsConfig,
    logs: LogsConfig,
}

#[derive(Deserialize)]
struct TracesConfig {
    endpoint: String,
}

#[derive(Deserialize)]
struct MetricsConfig {
    endpoint: String,
}

#[derive(Deserialize)]
struct LogsConfig {
    endpoint: String,
}

#[derive(Deserialize)]
struct LogConfig {
    level: String,
}

pub enum ConfigKey {
    ServerName,
    ServerPort,
    OtelExporterOtlpTracesEndpoint,
    OtelExporterOtlpMetricsEndpoint,
    OtelExporterOtlpLogsEndpoint,
    RustLog,
}

// 全局配置实例
static APP_CONFIG: OnceLock<AppConfig> = OnceLock::new();

/// 初始化配置
/// 按优先级加载: 环境变量 > config.yaml > 默认值
pub fn init_config() -> Result<(), ConfigError> {
    const DEFAULT_CONFIG: &str = include_str!("../../application.yml");

    let config = Config::builder()
        .add_source(config::File::from_str(
            DEFAULT_CONFIG,
            config::FileFormat::Yaml,
        ))
        .add_source(Environment::default().separator("_").try_parsing(true))
        .build()?;

    let app_config: AppConfig = config.try_deserialize()?;
    APP_CONFIG
        .set(app_config)
        .map_err(|_| ConfigError::Message("Config already initialized".to_string()))?;

    Ok(())
}

/// 获取配置实例
fn get_app_config() -> &'static AppConfig {
    APP_CONFIG.get().expect("Config should be initialized")
}

/// 从配置获取值
pub fn get_config(key: ConfigKey) -> String {
    let config = get_app_config();
    match key {
        ConfigKey::ServerName => config.server.name.clone(),
        ConfigKey::ServerPort => config.server.port.to_string(),
        ConfigKey::OtelExporterOtlpTracesEndpoint => {
            config.otel.exporter.otlp.traces.endpoint.clone()
        }
        ConfigKey::OtelExporterOtlpMetricsEndpoint => {
            config.otel.exporter.otlp.metrics.endpoint.clone()
        }
        ConfigKey::OtelExporterOtlpLogsEndpoint => config.otel.exporter.otlp.logs.endpoint.clone(),
        ConfigKey::RustLog => config.log.level.clone(),
    }
}

/// 获取静态字符串引用
pub fn get_static_value(key: ConfigKey) -> &'static str {
    Box::leak(get_config(key).into_boxed_str())
}
