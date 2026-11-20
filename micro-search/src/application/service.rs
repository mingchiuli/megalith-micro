use crate::domain::IndexKind;
use crate::{
    adapter::outbound::{meili::Meili, store::Store},
    config::Settings,
    domain::{self, AdminChange, BlogOperation, PublicChange, number},
};
use anyhow::{Context, Result};
use reqwest::{Client, Method};
use serde::Deserialize;
use serde_json::{Value, json};
use std::{collections::BTreeMap, sync::Arc};

#[derive(Clone)]
pub struct Search {
    pub cfg: Arc<Settings>,
    pub engine: Meili,
    pub store: Store,
    pub http: Client,
    pub broker: crate::adapter::outbound::broker::Broker,
}
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ReadCount {
    pub blog_id: i64,
    pub read_count: i64,
}
impl Search {
    pub fn task_lock(&self, task: &str) -> String {
        format!("megalith:{}:task:{task}", self.cfg.environment)
    }
    pub fn public_lock(&self) -> String {
        format!(
            "megalith:search:{}:snapshot",
            self.cfg.index_uid(IndexKind::Public)
        )
    }
    pub async fn initialize(&self) -> Result<()> {
        self.store
            .locked(self.task_lock("search:index-rebuild"), false, async {
                for kind in IndexKind::ALL {
                    self.engine
                        .configure(kind, self.cfg.index_uid(kind), self.cfg.max_hits)
                        .await?;
                }
                Ok(())
            })
            .await
    }
    pub async fn public_event(&self, event: PublicChange) -> Result<()> {
        let blog = &event.blog_snapshot;
        let id = number(blog, "id")?;
        let revision = event.revision;
        let index = self.cfg.index_uid(IndexKind::Public);
        self.store
            .locked(self.public_lock(), true, async {
                self.engine.drain(index).await?;
                let old = self.engine.doc(index, id).await?;
                if old["revision"].as_i64().is_some_and(|v| v >= revision) {
                    return Ok(());
                }
                let doc = match event.operation {
                    BlogOperation::Create | BlogOperation::Update => {
                        let mut doc = domain::public_document(blog, revision)?;
                        if !old.is_null() {
                            doc["readCount"] = old["readCount"]
                                .as_i64()
                                .unwrap_or(0)
                                .max(doc["readCount"].as_i64().unwrap_or(0))
                                .into();
                        }
                        doc
                    }
                    BlogOperation::Remove => domain::public_tombstone(id, revision),
                };
                self.engine.write(index, vec![doc]).await
            })
            .await
    }
    pub async fn admin_event(&self, event: AdminChange) -> Result<()> {
        let admin = self.cfg.index_uid(IndexKind::Admin);
        let rev = self.cfg.index_uid(IndexKind::Revision);
        let id = number(&event.blog, "id")?;
        self.store
            .locked(format!("megalith:search:{admin}:{id}:lock"), false, async {
                let mut versions = Vec::new();
                for row in [event.revision.as_ref(), event.published_revision.as_ref()]
                    .into_iter()
                    .flatten()
                {
                    if !versions.iter().any(|v: &Value| v["id"] == row["id"]) {
                        versions.push(domain::revision_document(row)?);
                    }
                }
                self.engine.write(rev, versions).await?;
                self.engine.drain(admin).await?;
                let old = self.engine.doc(admin, id).await?;
                let incoming = number(&event.blog, "adminRevision")?;
                if old["adminRevision"].as_i64().is_some_and(|v| {
                    v > incoming || (v == incoming && event.operation != "RECOVER")
                }) {
                    return Ok(());
                }
                self.engine
                    .write(admin, vec![domain::admin_document(&event, &old)?])
                    .await
            })
            .await
    }
    pub async fn update_counts(&self, counts: Vec<ReadCount>) -> Result<()> {
        let mut requested = BTreeMap::<i64, i64>::new();
        for c in counts {
            requested
                .entry(c.blog_id)
                .and_modify(|n| *n = (*n).max(c.read_count))
                .or_insert(c.read_count);
        }
        let index = self.cfg.index_uid(IndexKind::Public);
        self.store
            .locked(self.public_lock(), true, async {
                self.engine.drain(index).await?;
                let fetch = json!({
                    "ids": requested.keys().collect::<Vec<_>>(),
                    "filter": "deleted = false",
                    "limit": requested.len(),
                    "fields": ["id", "readCount"]
                });
                let docs = self
                    .engine
                    .post(&format!("/indexes/{index}/documents/fetch"), fetch)
                    .await?;
                let mut updates = Vec::new();
                for row in domain::rows(&docs["results"])? {
                    let id = number(row, "id")?;
                    let count = *requested.get(&id).context("unexpected document id")?;
                    if count > row["readCount"].as_i64().unwrap_or(0) {
                        updates.push(json!({"id": id, "readCount": count}));
                    }
                }
                if !updates.is_empty() {
                    self.engine
                        .task(
                            Method::PUT,
                            &format!("/indexes/{index}/documents"),
                            updates.into(),
                        )
                        .await?;
                }
                Ok(())
            })
            .await
    }
}
