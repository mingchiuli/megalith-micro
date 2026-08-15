use config::{Config, ConfigError, Environment};
use serde::Deserialize;
use std::sync::OnceLock;

#[derive(Deserialize)]
struct AppConfig {
    server: ServerConfig,
    otel: OtelConfig,
    log: LogConfig,
    megalith: MegalithConfig,
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

#[derive(Debug, Deserialize)]
struct ExporterConfig {
    otlp: OtlpConfig,
}

#[derive(Debug, Deserialize)]
struct OtlpConfig {
    traces: TracesConfig,
    metrics: MetricsConfig,
    logs: LogsConfig,
}

#[derive(Debug, Deserialize)]
struct TracesConfig {
    endpoint: String,
}

#[derive(Debug, Deserialize)]
struct MetricsConfig {
    endpoint: String,
}

#[derive(Debug, Deserialize)]
struct LogsConfig {
    endpoint: String,
}

#[derive(Debug, Deserialize)]
struct LogConfig {
    level: String,
}

#[derive(Debug, Deserialize)]
struct MegalithConfig {
    blog: BlogConfig,
}

#[derive(Debug, Deserialize)]
struct BlogConfig {
    auth: AuthConfig,
    frontend: FrontendConfig,
}

#[derive(Debug, Deserialize)]
struct AuthConfig {
    url: String,
    timeout_ms: u64,
    max_inflight: usize,
}

#[derive(Debug, Deserialize)]
struct FrontendConfig {
    allowed_origins: String,
}

pub enum ConfigKey {
    ServerName,
    ServerPort,
    OtelExporterOtlpTracesEndpoint,
    OtelExporterOtlpMetricsEndpoint,
    OtelExporterOtlpLogsEndpoint,
    AuthUrlKey,
    AuthTimeoutMs,
    AuthMaxInflight,
    AllowedOrigins,
    RustLog,
}

// 全局配置实例
static APP_CONFIG: OnceLock<AppConfig> = OnceLock::new();

/// 初始化配置
/// 按优先级加载: 环境变量 > config.yaml > 默认值
pub fn init_config() -> Result<(), ConfigError> {
    const DEFAULT_CONFIG: &str = include_str!("../application.yml");

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
        ConfigKey::AuthUrlKey => config.megalith.blog.auth.url.clone(),
        ConfigKey::AuthTimeoutMs => config.megalith.blog.auth.timeout_ms.to_string(),
        ConfigKey::AuthMaxInflight => config.megalith.blog.auth.max_inflight.to_string(),
        ConfigKey::AllowedOrigins => config.megalith.blog.frontend.allowed_origins.clone(),
        ConfigKey::RustLog => config.log.level.clone(),
    }
}

/// 服务名静态引用，直接从全局配置借用，无需 clone 或泄漏
pub fn server_name() -> &'static str {
    get_app_config().server.name.as_str()
}
