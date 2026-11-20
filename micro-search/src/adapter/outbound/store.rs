use anyhow::{Result, ensure};
use redis::{Script, aio::ConnectionManager};
use std::{future::Future, time::Duration};
use uuid::Uuid;

#[derive(Clone)]
pub struct Store(pub ConnectionManager);
impl Store {
    pub async fn locked<T>(
        &self,
        key: String,
        write_lock: bool,
        work: impl Future<Output = Result<T>>,
    ) -> Result<T> {
        let mut conn = self.0.clone();
        let owner = Uuid::new_v4().to_string();
        let ttl = 30_000;
        let acquire = Script::new(include_str!("lua/acquire.lua"));
        loop {
            let ok: i32 = acquire
                .key(&key)
                .arg(&owner)
                .arg(ttl)
                .arg(if write_lock { "write" } else { "exclusive" })
                .invoke_async(&mut conn)
                .await?;
            if ok == 1 {
                break;
            }
            tokio::time::sleep(Duration::from_millis(50)).await;
        }
        tokio::pin!(work);
        let renew = Script::new(include_str!("lua/renew.lua"));
        let result = loop {
            tokio::select! {
                output = &mut work => break output,
                () = tokio::time::sleep(Duration::from_secs(5)) => {
                    let mut heartbeat = renew.key(&key);
                    heartbeat.arg(&owner).arg(ttl);
                    let renewed = tokio::time::timeout(
                        Duration::from_secs(5), heartbeat.invoke_async::<i32>(&mut conn),
                    ).await??;
                    ensure!(renewed == 1, "Redis lock ownership lost");
                }
            }
        };
        let prefix = if write_lock {
            "redisson_rwlock"
        } else {
            "redisson_lock__channel"
        };
        let channel = format!("{prefix}:{{{key}}}");
        let released: i32 = Script::new(include_str!("lua/release.lua"))
            .key(key)
            .arg(owner)
            .arg(channel)
            .arg(i32::from(write_lock))
            .invoke_async(&mut conn)
            .await?;
        ensure!(released == 1, "Redis lock expired");
        result
    }
}
