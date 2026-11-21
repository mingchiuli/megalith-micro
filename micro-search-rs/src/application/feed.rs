use super::Search;
use super::contract::RecommendationFeed;
use crate::domain;
use crate::domain::IndexKind;
use anyhow::{Context, Result, bail};
use base64::{Engine, engine::general_purpose::URL_SAFE_NO_PAD};
use jiff::Timestamp;
use serde::{Deserialize, Serialize};
use serde_json::Value;
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
) -> Result<RecommendationFeed> {
    let has_more = rows.len() > size;
    rows.truncate(size);
    let next_cursor = if has_more {
        let cursor = Cursor {
            version: 2,
            snapshot,
            offset: offset.checked_add(size as i32).context("cursor overflow")?,
        };
        Some(URL_SAFE_NO_PAD.encode(serde_json::to_vec(&cursor)?))
    } else {
        None
    };
    Ok(RecommendationFeed {
        content: rows,
        next_cursor,
        has_more,
        restart,
    })
}
impl Search {
    async fn cached_first_page(
        &self,
        index: &str,
        key: &str,
        size: usize,
        restart: bool,
    ) -> Result<Option<RecommendationFeed>> {
        let Some(id) = self.store.snapshot_id(key).await? else {
            return Ok(None);
        };
        if id.is_nil() {
            return Ok(Some(page(Vec::new(), size, id, 0, restart)?));
        }
        self.store
            .snapshot_page(index, id, 0, size, self.cfg.snapshot_ttl)
            .await?
            .map(|rows| page(rows, size, id, 0, restart))
            .transpose()
    }

    pub async fn feed(
        &self,
        cursor: Option<Cursor>,
        size: usize,
        restart: bool,
    ) -> Result<RecommendationFeed> {
        let index = self.cfg.index_uid(IndexKind::Public);
        if let Some(c) = cursor
            && let Some(rows) = self
                .store
                .snapshot_page(index, c.snapshot, c.offset, size, self.cfg.snapshot_ttl)
                .await?
        {
            return page(rows, size, c.snapshot, c.offset, false);
        }
        loop {
            let generation = self
                .store
                .locked(self.public_lock(), true, self.engine.generation(index))
                .await?;
            let key = format!("megalith:search:{index}:cards:{generation}");
            if let Some(reply) = self.cached_first_page(index, &key, size, restart).await? {
                return Ok(reply);
            }
            let reply = self
                .store
                .locked(format!("{key}:build"), false, async {
                    if let Some(reply) = self.cached_first_page(index, &key, size, restart).await? {
                        return Ok(Some(reply));
                    }
                    let candidates = self
                        .store
                        .locked(self.public_lock(), true, async {
                            if self.engine.generation(index).await? != generation {
                                return Ok(None);
                            }
                            Ok(Some(self.engine.recommendation_candidates(index).await?))
                        })
                        .await?;
                    let Some(rows) = candidates else {
                        return Ok(None);
                    };
                    let rows = tokio::task::spawn_blocking(move || {
                        domain::rank_recommendations(rows, Timestamp::now().as_millisecond())
                    })
                    .await??;
                    let id = self
                        .store
                        .save_snapshot(index, &key, &rows, self.cfg.snapshot_ttl)
                        .await?;
                    Ok(Some(page(rows, size, id, 0, restart)?))
                })
                .await?;
            if let Some(reply) = reply {
                return Ok(reply);
            }
        }
    }
}
