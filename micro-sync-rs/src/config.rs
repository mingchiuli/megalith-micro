use config::{Config, ConfigError, Environment};
use serde::Deserialize;
use std::sync::OnceLock;

#[derive(Clone, Deserialize)]
struct AppConfig {
    server: ServerConfig,
    redis: RedisConfig,
    sync: SyncConfig,
    worker: WorkerConfig,
    otel: OtelConfig,
    log: LogConfig,
}

#[derive(Clone, Deserialize)]
struct ServerConfig {
    name: String,
    port: u16,
}

#[derive(Clone, Deserialize)]
pub struct RedisConfig {
    pub url: String,
    pub prefix: String,
}

#[derive(Clone, Deserialize)]
pub struct SyncConfig {
    pub session_retention_seconds: u64,
    pub lease_heartbeat_seconds: u64,
    pub lease_timeout_seconds: u64,
    pub stream_safety_seconds: u64,
    pub relay_block_millis: usize,
    pub relay_batch_size: usize,
    pub connection_buffer: usize,
    pub initial_sync_timeout_seconds: u64,
}

#[derive(Clone, Deserialize)]
pub struct WorkerConfig {
    pub task_debounce_seconds: u64,
    pub task_timeout_seconds: u64,
    pub concurrency: usize,
}

#[derive(Clone, Deserialize)]
struct OtelConfig {
    exporter: ExporterConfig,
}

#[derive(Clone, Deserialize)]
struct ExporterConfig {
    otlp: OtlpConfig,
}

#[derive(Clone, Deserialize)]
struct OtlpConfig {
    traces: TracesConfig,
    metrics: MetricsConfig,
    logs: LogsConfig,
}

#[derive(Clone, Deserialize)]
struct TracesConfig {
    endpoint: String,
}

#[derive(Clone, Deserialize)]
struct MetricsConfig {
    endpoint: String,
}

#[derive(Clone, Deserialize)]
struct LogsConfig {
    endpoint: String,
}

#[derive(Clone, Deserialize)]
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
    const DEFAULT_CONFIG: &str = include_str!("../application.yml");

    let config = Config::builder()
        .add_source(config::File::from_str(
            DEFAULT_CONFIG,
            config::FileFormat::Yaml,
        ))
        .add_source(Environment::default().separator("_").try_parsing(true))
        .add_source(Environment::default().separator("__").try_parsing(true))
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

pub fn redis_config() -> RedisConfig {
    get_app_config().redis.clone()
}

pub fn sync_config() -> SyncConfig {
    get_app_config().sync.clone()
}

pub fn worker_config() -> WorkerConfig {
    get_app_config().worker.clone()
}
