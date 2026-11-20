use super::Search;
use crate::domain;
use crate::domain::IndexKind;
use anyhow::{Context, Result, bail};
use base64::{Engine, engine::general_purpose::URL_SAFE_NO_PAD};
use jiff::Timestamp;
use redis::AsyncCommands;
use serde::{Deserialize, Serialize};
use serde_json::{Value, json};
use uuid::Uuid;

#[derive(Serialize, Deserialize)]
pub struct Cursor {
    version: u8,
    snapshot: Uuid,
    offset: i32,
}
pub fn decode(value: &str) -> Result<Option<Cursor>> {
    if value.len() > 4096 {
        bail!("invalid recommendation cursor");
    }
    let raw = URL_SAFE_NO_PAD.decode(value)?;
    let node: Value = serde_json::from_slice(&raw)?;
    if node["version"] == 1 && node["pitId"].as_str().is_some_and(|v| !v.trim().is_empty()) {
        return Ok(None);
    }
    let c: Cursor = serde_json::from_value(node)?;
    if c.version != 2 || !(1..i32::MAX - 1000).contains(&c.offset) {
        bail!("invalid recommendation cursor");
    }
    Ok(Some(c))
}
fn page(
    mut rows: Vec<Value>,
    size: usize,
    snapshot: Uuid,
    offset: i32,
    restart: bool,
) -> Result<Value> {
    let more = rows.len() > size;
    rows.truncate(size);
    let mut reply = json!({"content":rows,"hasMore":more,"restart":restart});
    if more {
        reply["nextCursor"] = URL_SAFE_NO_PAD
            .encode(serde_json::to_vec(&Cursor {
                version: 2,
                snapshot,
                offset: offset.checked_add(size as i32).context("cursor overflow")?,
            })?)
            .into();
    }
    Ok(reply)
}
impl Search {
    async fn saved_page(
        &self,
        id: Uuid,
        offset: i32,
        size: usize,
        restart: bool,
    ) -> Result<Option<Value>> {
        let key = self.feed_key(id);
        let mut pipe = redis::pipe();
        pipe.atomic()
            .cmd("LRANGE")
            .arg(&key)
            .arg(offset)
            .arg(offset + size as i32)
            .cmd("PEXPIRE")
            .arg(&key)
            .arg(self.cfg.snapshot_ttl.as_millis() as u64)
            .ignore();
        let (raw,): (Vec<String>,) = pipe.query_async(&mut self.store.0.clone()).await?;
        if raw.is_empty() {
            return Ok(None);
        }
        let rows = raw
            .iter()
            .map(|row| serde_json::from_str(row))
            .collect::<Result<_, _>>()?;
        Ok(Some(page(rows, size, id, offset, restart)?))
    }
    fn feed_key(&self, id: Uuid) -> String {
        format!(
            "megalith:search:{}:feed:{id}",
            self.cfg.index_uid(IndexKind::Public)
        )
    }
    pub async fn feed(&self, cursor: Option<Cursor>, size: usize, restart: bool) -> Result<Value> {
        if let Some(c) = cursor
            && let Some(reply) = self.saved_page(c.snapshot, c.offset, size, false).await?
        {
            return Ok(reply);
        }
        self.store
            .locked(self.public_lock(), true, self.first_page(size, restart))
            .await
    }

    async fn first_page(&self, size: usize, restart: bool) -> Result<Value> {
        let index = self.cfg.index_uid(IndexKind::Public);
        self.engine.drain(index).await?;
        let meta = self.engine.get(&format!("/indexes/{index}")).await?;
        let generation = meta["updatedAt"]
            .as_str()
            .context("missing index generation")?;
        let key = format!("megalith:search:{index}:generation:{generation}");
        let mut conn = self.store.0.clone();
        if let Some(id) = conn.get::<_, Option<String>>(&key).await?
            && let Some(reply) = self.saved_page(id.parse()?, 0, size, restart).await?
        {
            return Ok(reply);
        }

        let rows = self.ranked_snapshot(index).await?;
        let id = Uuid::new_v4();
        if rows.len() > size {
            let ttl = self.cfg.snapshot_ttl.as_millis() as u64;
            let list = self.feed_key(id);
            for chunk in rows.chunks(128) {
                let values = chunk
                    .iter()
                    .map(serde_json::to_string)
                    .collect::<Result<Vec<_>, _>>()?;
                redis::pipe()
                    .atomic()
                    .cmd("RPUSH")
                    .arg(&list)
                    .arg(values)
                    .ignore()
                    .cmd("PEXPIRE")
                    .arg(&list)
                    .arg(ttl)
                    .ignore()
                    .query_async::<()>(&mut conn)
                    .await?;
            }
            conn.pset_ex::<_, _, ()>(&key, id.to_string(), ttl).await?;
        }
        page(rows, size, id, 0, restart)
    }

    async fn ranked_snapshot(&self, index: &str) -> Result<Vec<Value>> {
        let now = Timestamp::now().as_millisecond();
        let mut ranked = Vec::new();
        loop {
            let fetch = json!({
                "filter": "deleted = false AND status = 0",
                "offset": ranked.len(), "limit": 256
            });
            let batch = self
                .engine
                .post(&format!("/indexes/{index}/documents/fetch"), fetch)
                .await?;
            let docs = domain::rows(&batch["results"])?;
            for doc in docs {
                let published = doc["publishedAt"]
                    .as_str()
                    .map(domain::millis)
                    .transpose()?
                    .unwrap_or(now);
                let days = now.abs_diff(published) as f64 / 86_400_000.;
                let distance = (days - 30.).max(0.) / 180.;
                let reads = doc["readCount"].as_u64().unwrap_or(0);
                let score = (reads as f64).ln_1p() * 0.5f64.powf(distance * distance);
                ranked.push((score, domain::number(doc, "id")?, domain::public_read(doc)));
            }
            if docs.len() < 256 {
                break;
            }
        }
        ranked.sort_by(|a, b| b.0.total_cmp(&a.0).then_with(|| a.1.cmp(&b.1)));
        Ok(ranked.into_iter().map(|(_, _, row)| row).collect())
    }
}
