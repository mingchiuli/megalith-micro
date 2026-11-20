use super::Search;
use crate::domain::IndexKind;
use crate::{adapter::outbound::broker, domain};
use anyhow::{Context, Result, ensure};
use reqwest::Method;
use serde::Deserialize;
use serde_json::{Value, json};
use uuid::Uuid;

#[derive(Debug)]
pub struct Conflict(pub String);
impl std::fmt::Display for Conflict {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str(&self.0)
    }
}
impl std::error::Error for Conflict {}
#[derive(Deserialize, PartialEq)]
#[serde(rename_all = "camelCase")]
struct Status {
    read_only: bool,
    ready_events: u64,
    paused_events: u64,
    total: u64,
    published_total: u64,
}
impl Search {
    async fn source(&self, suffix: &str) -> Result<Value> {
        let body: Value = self
            .http
            .get(format!("{}/blog/index/{suffix}", self.cfg.blog))
            .headers(megalith_otel::trace_headers())
            .send()
            .await?
            .error_for_status()?
            .json()
            .await?;
        ensure!(body["code"] == 200, "blog source rejected snapshot read");
        Ok(body["data"].clone())
    }
    async fn status(&self) -> Result<Status> {
        let status: Status = serde_json::from_value(self.source("status").await?)?;
        ensure!(
            status.read_only && status.ready_events == 0 && status.paused_events == 0,
            Conflict("blog source must be read-only with an empty BLOG outbox".into())
        );
        Ok(status)
    }
    pub async fn rebuild(&self) -> Result<Value> {
        ensure!(
            self.cfg.maintenance,
            Conflict("search index rebuild requires maintenance mode".into())
        );
        self.store
            .locked(
                self.task_lock("search:index-rebuild"),
                false,
                self.store.locked(
                    self.task_lock("blog:search-read-count-sync"),
                    false,
                    self.rebuild_locked(),
                ),
            )
            .await
    }
    async fn rebuild_locked(&self) -> Result<Value> {
        broker::quiescent(&self.broker).await?;
        let before = self.status().await?;
        let mut result = Vec::new();
        let mut swaps = Vec::new();
        for kind in IndexKind::ALL {
            let route = kind.snapshot_route();
            let live = self.cfg.index_uid(kind);
            let stage = format!("{live}-{}", Uuid::new_v4());
            self.engine
                .configure(kind, &stage, self.cfg.max_hits)
                .await?;
            let cursor = if matches!(kind, IndexKind::Revision) {
                "afterRevisionId"
            } else {
                "afterId"
            };
            let mut after = 0;
            let mut copied = 0;
            loop {
                let response = self
                    .source(&format!(
                        "{route}?{cursor}={after}&limit={}",
                        self.cfg.batch
                    ))
                    .await?;
                let rows = domain::rows(&response)?;
                if rows.is_empty() {
                    break;
                }
                ensure!(
                    rows.len() <= self.cfg.batch,
                    Conflict("oversized snapshot batch".into())
                );
                let mut docs = Vec::new();
                for row in rows {
                    let id = domain::source_id(row, kind)?;
                    ensure!(id > after, Conflict("invalid snapshot order".into()));
                    after = id;
                    docs.push(match kind {
                        IndexKind::Public => {
                            let rev = domain::number(row, "revision")?;
                            ensure!(rev > 0, Conflict("invalid publication revision".into()));
                            domain::public_document(row, rev)?
                        }
                        IndexKind::Admin => domain::admin_document(
                            &serde_json::from_value(row.clone())?,
                            &Value::Null,
                        )?,
                        IndexKind::Revision => domain::revision_document(row)?,
                    });
                }
                copied += docs.len() as u64;
                self.engine.write(&stage, docs).await?;
            }
            let stats = self.engine.get(&format!("/indexes/{stage}/stats")).await?;
            ensure!(
                stats["numberOfDocuments"]
                    .as_u64()
                    .context("missing index count")?
                    == copied
                    && match kind {
                        IndexKind::Public => copied == before.published_total,
                        IndexKind::Admin => copied == before.total,
                        IndexKind::Revision => true,
                    },
                Conflict("source and rebuilt index counts do not agree".into())
            );
            swaps.push(json!({"indexes":[live,stage]}));
            result.push(json!({"previousIndex":stage,"index":live,"documents":copied}));
        }
        ensure!(
            before == self.status().await?,
            Conflict("blog source changed during rebuild".into())
        );
        broker::quiescent(&self.broker).await?;
        self.engine
            .task(Method::POST, "/swap-indexes", Value::Array(swaps))
            .await?;
        Ok(json!({"indexes":result}))
    }
}
