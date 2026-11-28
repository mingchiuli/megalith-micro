use super::Search;
use crate::domain::IndexKind;
use crate::{adapter::outbound::broker, domain};
use anyhow::{Result, ensure};
pub(crate) use megalith_api::blog::BlogIndexSourceStatus as Status;
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
impl Search {
    async fn ready_source(&self) -> Result<Status> {
        let status = self.source.status().await?;
        ensure!(
            status.ready(),
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
        let before = self.ready_source().await?;
        let mut stages = Vec::new();
        let mut swap_started = false;
        let result = async {
            let report = self.prepare_stages(&before, &mut stages).await?;
            self.store
                .locked(self.public_lock(), true, async {
                    swap_started = true;
                    self.engine.swap(&stages).await
                })
                .await?;
            Ok(json!({"indexes":report}))
        }
        .await;
        if result.is_err() && !swap_started {
            for (_, stage) in &stages {
                if let Err(cleanup) = self.engine.delete_stage(stage).await {
                    tracing::error!(stage, %cleanup, "could not remove staging index");
                }
            }
        }
        result
    }

    async fn prepare_stages(
        &self,
        before: &Status,
        stages: &mut Vec<(String, String)>,
    ) -> Result<Vec<Value>> {
        let mut result = Vec::new();
        for kind in IndexKind::ALL {
            let live = self.cfg.index_uid(kind);
            let stage = format!("{live}-{}", Uuid::new_v4());
            stages.push((live.to_owned(), stage.clone()));
            self.engine
                .configure(kind, &stage, self.cfg.max_hits)
                .await?;
            let mut after = 0;
            let mut copied = 0;
            loop {
                let response = self.source.page(kind, after, self.cfg.batch).await?;
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
                        IndexKind::Revision => domain::revision_document(
                            row,
                            domain::number(row, "historyGeneration")?,
                            &Value::Null,
                        )?,
                    });
                }
                copied += docs.len() as u64;
                self.engine.write(&stage, docs).await?;
            }
            ensure!(
                self.engine.document_count(&stage).await? == copied
                    && match kind {
                        IndexKind::Public => copied == before.published_total,
                        IndexKind::Admin => copied == before.total,
                        IndexKind::Revision => copied == before.revision_total,
                    },
                Conflict("source and rebuilt index counts do not agree".into())
            );
            result.push(json!({"previousIndex":stage,"index":live,"documents":copied}));
        }
        ensure!(
            *before == self.ready_source().await?,
            Conflict("blog source changed during rebuild".into())
        );
        broker::quiescent(&self.broker).await?;
        Ok(result)
    }
}
