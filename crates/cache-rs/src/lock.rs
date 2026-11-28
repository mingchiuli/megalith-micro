use std::time::Duration;

use anyhow::{Result, ensure};
use redis::aio::ConnectionManager;
use tokio::{
    task::JoinHandle,
    time::{Instant, sleep},
};
use uuid::Uuid;

const TTL_MS: u64 = 30_000;
const ACQUIRE: &str = include_str!("../script/lock-acquire.lua");
const RENEW: &str = include_str!("../script/lock-renew.lua");
const RELEASE: &str = include_str!("../script/lock-release.lua");

pub(crate) struct RemoteLock {
    connection: ConnectionManager,
    name: String,
    owner: String,
    watchdog: JoinHandle<()>,
    released: bool,
}

impl RemoteLock {
    pub async fn acquire(
        connection: ConnectionManager,
        key: &str,
        wait: Duration,
    ) -> Result<Option<Self>> {
        let name = format!("megalithRemoteLock:{key}");
        let owner = format!("{}:1", Uuid::new_v4());
        let deadline = Instant::now() + wait;
        loop {
            let acquired: bool = redis::cmd("EVAL")
                .arg(ACQUIRE)
                .arg(1)
                .arg(&name)
                .arg(&owner)
                .arg(TTL_MS)
                .query_async(&mut connection.clone())
                .await?;
            if acquired {
                break;
            }
            if Instant::now() >= deadline {
                return Ok(None);
            }
            sleep((deadline - Instant::now()).min(Duration::from_millis(20))).await;
        }
        let watchdog = tokio::spawn(renew(connection.clone(), name.clone(), owner.clone()));
        Ok(Some(Self {
            connection,
            name,
            owner,
            watchdog,
            released: false,
        }))
    }

    pub async fn release(mut self) -> Result<()> {
        self.watchdog.abort();
        release(self.connection.clone(), &self.name, &self.owner).await?;
        self.released = true;
        Ok(())
    }
}

impl Drop for RemoteLock {
    fn drop(&mut self) {
        self.watchdog.abort();
        if !self.released
            && let Ok(runtime) = tokio::runtime::Handle::try_current()
        {
            let connection = self.connection.clone();
            let name = self.name.clone();
            let owner = self.owner.clone();
            runtime.spawn(async move {
                let _ = release(connection, &name, &owner).await;
            });
        }
    }
}

async fn renew(mut connection: ConnectionManager, name: String, owner: String) {
    loop {
        sleep(Duration::from_millis(TTL_MS / 3)).await;
        let renewed = redis::cmd("EVAL")
            .arg(RENEW)
            .arg(1)
            .arg(&name)
            .arg(&owner)
            .arg(TTL_MS)
            .query_async::<bool>(&mut connection)
            .await;
        if !matches!(renewed, Ok(true)) {
            break;
        }
    }
}

async fn release(mut connection: ConnectionManager, name: &str, owner: &str) -> Result<()> {
    let channel = format!("redisson_lock__channel:{{{name}}}");
    let removed: bool = redis::cmd("EVAL")
        .arg(RELEASE)
        .arg(2)
        .arg(name)
        .arg(channel)
        .arg(owner)
        .query_async(&mut connection)
        .await?;
    ensure!(removed, "cache lock ownership expired");
    Ok(())
}
