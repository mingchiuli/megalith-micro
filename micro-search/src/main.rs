use anyhow::Result;
use micro_search::config::Settings;
use std::{env, ffi::OsStr, path::Path, time::Duration};

fn main() -> Result<()> {
    let cfg = Settings::load()?;
    let mut args = env::args_os();
    // Compose deployments using the Buildpacks probe invoke this binary through the thc symlink.
    let legacy_probe = args
        .next()
        .is_some_and(|path| Path::new(&path).file_name() == Some(OsStr::new("thc")));
    if legacy_probe || args.next().as_deref() == Some(OsStr::new("health")) {
        reqwest::blocking::Client::builder()
            .timeout(Duration::from_secs(4))
            .build()?
            .get(format!(
                "http://127.0.0.1:{}/actuator/health",
                cfg.bind.port()
            ))
            .send()?
            .error_for_status()?;
        return Ok(());
    }
    let ratio = std::env::var("management.tracing.sampling.probability")
        .or_else(|_| std::env::var("MANAGEMENT_TRACING_SAMPLING_PROBABILITY"))
        .ok()
        .and_then(|s| s.parse().ok())
        .unwrap_or(0.05);
    let filter = std::env::var("RUST_LOG").unwrap_or_else(|_| "info".into());
    let providers = micro_search::config::otlp_config().init(
        "micro-search",
        env!("CARGO_PKG_VERSION"),
        &filter,
        ratio,
    )?;
    let result = tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()?
        .block_on(micro_search::serve(cfg));
    providers.shutdown();
    result
}
