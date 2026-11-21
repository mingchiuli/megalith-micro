use anyhow::{Result, ensure};
use redis::{AsyncCommands, Script, aio::ConnectionManager};
use serde_json::Value;
use std::{future::Future, time::Duration};
use uuid::Uuid;

#[derive(Clone)]
pub struct Store(ConnectionManager);
impl Store {
    pub async fn connect(url: &str) -> Result<Self> {
        Ok(Self(
            redis::Client::open(url)?.get_connection_manager().await?,
        ))
    }

    pub async fn ready(&self) -> bool {
        redis::cmd("PING")
            .query_async::<String>(&mut self.0.clone())
            .await
            .is_ok()
    }

    pub async fn locked<T>(
        &self,
        key: String,
        write_lock: bool,
        work: impl Future<Output = Result<T>>,
    ) -> Result<T> {
        let mut conn = self.0.clone();
        let owner = Uuid::new_v4().to_string();
        let ttl = 30_000;
        let acquire = Script::new(include_str!("redis/scripts/acquire.lua"));
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
        let renew = Script::new(include_str!("redis/scripts/renew.lua"));
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
        let released: i32 = Script::new(include_str!("redis/scripts/release.lua"))
            .key(key)
            .arg(owner)
            .arg(channel)
            .arg(i32::from(write_lock))
            .invoke_async(&mut conn)
            .await?;
        ensure!(released == 1, "Redis lock expired");
        result
    }

    pub async fn snapshot_page(
        &self,
        index: &str,
        id: Uuid,
        offset: i32,
        size: usize,
        ttl: Duration,
    ) -> Result<Option<Vec<Value>>> {
        let key = format!("megalith:search:{index}:feed:{id}");
        let (raw,): (Vec<String>,) = redis::pipe()
            .atomic()
            .cmd("LRANGE")
            .arg(&key)
            .arg(offset)
            .arg(offset + size as i32)
            .cmd("PEXPIRE")
            .arg(&key)
            .arg(ttl.as_millis() as u64)
            .ignore()
            .query_async(&mut self.0.clone())
            .await?;
        if raw.is_empty() {
            return Ok(None);
        }
        let rows = raw
            .iter()
            .map(|raw| {
                let mut card: Value = serde_json::from_str(raw)?;
                // Existing v2 cursors may point to lists containing the former full article payload.
                if let Some(fields) = card.as_object_mut() {
                    fields.retain(|key, _| {
                        crate::domain::RECOMMENDATION_FIELDS.contains(&key.as_str())
                    });
                }
                Ok::<_, serde_json::Error>(card)
            })
            .collect::<Result<_, _>>()?;
        Ok(Some(rows))
    }

    pub async fn snapshot_id(&self, key: &str) -> Result<Option<Uuid>> {
        let raw: Option<String> = self.0.clone().get(key).await?;
        Ok(raw.map(|id| id.parse()).transpose()?)
    }

    pub async fn save_snapshot(
        &self,
        index: &str,
        generation_key: &str,
        rows: &[Value],
        ttl: Duration,
    ) -> Result<Uuid> {
        let mut conn = self.0.clone();
        let ttl = ttl.as_millis() as u64;
        // The nil UUID caches an empty generation. It never appears in a cursor.
        let id = if rows.is_empty() {
            Uuid::nil()
        } else {
            Uuid::new_v4()
        };
        let list = format!("megalith:search:{index}:feed:{id}");
        let mut expected = 0;
        for chunk in rows.chunks(128) {
            let values = chunk
                .iter()
                .map(serde_json::to_string)
                .collect::<Result<Vec<_>, _>>()?;
            let (length,): (usize,) = redis::pipe()
                .atomic()
                .cmd("RPUSH")
                .arg(&list)
                .arg(values)
                .cmd("PEXPIRE")
                .arg(&list)
                .arg(ttl)
                .ignore()
                .query_async(&mut conn)
                .await?;
            expected += chunk.len();
            ensure!(
                length == expected,
                "recommendation list expired during staging"
            );
        }
        conn.pset_ex::<_, _, ()>(generation_key, id.to_string(), ttl)
            .await?;
        Ok(id)
    }
}
