use crate::config::Settings;
use crate::domain::IndexKind;
use anyhow::{Context, Result, bail};
use reqwest::{Client, Method, StatusCode};
use serde_json::{Value, json};
use std::time::Duration;

#[derive(Clone)]
pub struct Meili {
    http: Client,
    base: String,
    key: String,
    timeout: Duration,
}
impl Meili {
    pub fn new(http: Client, c: &Settings) -> Self {
        Self {
            http,
            base: c.engine.clone(),
            key: c.key.clone(),
            timeout: c.task_timeout,
        }
    }
    async fn send(
        &self,
        method: Method,
        path: &str,
        body: Option<&Value>,
        missing: Option<&str>,
    ) -> Result<Option<Value>> {
        let mut req = self
            .http
            .request(method, format!("{}{path}", self.base))
            .headers(megalith_otel::trace_headers());
        if !self.key.is_empty() {
            req = req.bearer_auth(&self.key);
        }
        if let Some(b) = body {
            req = req.json(b);
        }
        let res = req.send().await?;
        let status = res.status();
        let value: Value = res.json().await?;
        if !status.is_success() {
            if status == StatusCode::NOT_FOUND
                && missing.is_some()
                && missing == value["code"].as_str()
            {
                return Ok(None);
            }
            bail!("Meilisearch {path}: {status} ({})", value["code"]);
        }
        Ok(Some(value))
    }
    pub async fn get(&self, path: &str) -> Result<Value> {
        self.send(Method::GET, path, None, None)
            .await?
            .context("missing response")
    }
    pub async fn post(&self, path: &str, body: Value) -> Result<Value> {
        self.send(Method::POST, path, Some(&body), None)
            .await?
            .context("missing response")
    }
    pub async fn doc(&self, index: &str, id: i64) -> Result<Value> {
        Ok(self
            .send(
                Method::GET,
                &format!("/indexes/{index}/documents/{id}"),
                None,
                Some("document_not_found"),
            )
            .await?
            .unwrap_or(Value::Null))
    }
    async fn wait(&self, id: u64) -> Result<()> {
        tokio::time::timeout(self.timeout, async {
            loop {
                let task = self.get(&format!("/tasks/{id}")).await?;
                match task["status"].as_str() {
                    Some("succeeded") => return Ok(()),
                    Some("enqueued" | "processing") => {
                        tokio::time::sleep(Duration::from_millis(25)).await
                    }
                    _ => bail!(
                        "Meilisearch task {id} did not succeed: {}",
                        task["error"]["code"]
                    ),
                }
            }
        })
        .await
        .with_context(|| format!("Meilisearch task {id} pending"))?
    }
    pub async fn task(&self, method: Method, path: &str, body: Value) -> Result<()> {
        let r = self
            .send(method, path, Some(&body), None)
            .await?
            .context("missing task")?;
        let uid = r["taskUid"].as_u64().context("missing task UID")?;
        if path == "/swap-indexes" {
            tracing::info!(uid, "index swap task accepted");
        }
        self.wait(uid).await
    }
    pub async fn swap(&self, stages: &[(String, String)]) -> Result<()> {
        let swaps: Vec<_> = stages
            .iter()
            .map(|(live, stage)| json!({"indexes":[live,stage]}))
            .collect();
        tracing::info!(?stages, "submitting index swap");
        self.task(Method::POST, "/swap-indexes", swaps.into()).await
    }

    pub async fn document_count(&self, index: &str) -> Result<u64> {
        self.get(&format!("/indexes/{index}/stats")).await?["numberOfDocuments"]
            .as_u64()
            .context("missing index count")
    }

    pub async fn generation(&self, index: &str) -> Result<String> {
        self.drain(index).await?;
        Ok(self.get(&format!("/indexes/{index}")).await?["updatedAt"]
            .as_str()
            .context("missing index generation")?
            .to_owned())
    }

    pub async fn recommendation_candidates(&self, index: &str) -> Result<Vec<Value>> {
        let mut rows = Vec::new();
        loop {
            let batch = self
                .post(
                    &format!("/indexes/{index}/documents/fetch"),
                    json!({
                        "filter":"deleted = false AND status = 0", "offset":rows.len(), "limit":256,
                        "fields": crate::domain::RECOMMENDATION_FIELDS
                    }),
                )
                .await?;
            let docs = crate::domain::rows(&batch["results"])?;
            rows.extend_from_slice(docs);
            if docs.len() < 256 {
                return Ok(rows);
            }
        }
    }

    pub async fn drain(&self, index: &str) -> Result<()> {
        let tasks = self
            .get(&format!(
                "/tasks?indexUids={index}&statuses=enqueued,processing&limit=1"
            ))
            .await?;
        if let Some(t) = tasks["results"]
            .as_array()
            .context("missing tasks")?
            .first()
        {
            self.wait(t["uid"].as_u64().context("invalid task")?)
                .await?;
        }
        Ok(())
    }
    pub async fn delete_stage(&self, index: &str) -> Result<()> {
        if let Some(task) = self
            .send(
                Method::DELETE,
                &format!("/indexes/{index}"),
                None,
                Some("index_not_found"),
            )
            .await?
        {
            self.wait(
                task["taskUid"]
                    .as_u64()
                    .context("missing deletion task UID")?,
            )
            .await?;
        }
        Ok(())
    }

    pub async fn delete_history(&self, index: &str, article: i64, generation: i64) -> Result<()> {
        self.task(
            Method::POST,
            &format!("/indexes/{index}/documents/delete"),
            json!({"filter": format!("articleKey = b{article} AND historyKey = g{generation}")}),
        )
        .await
    }

    pub async fn write(&self, index: &str, rows: Vec<Value>) -> Result<()> {
        if !rows.is_empty() {
            self.task(
                Method::POST,
                &format!("/indexes/{index}/documents"),
                Value::Array(rows),
            )
            .await?;
        }
        Ok(())
    }
    pub async fn configure(&self, kind: IndexKind, name: &str, max: usize) -> Result<()> {
        let path = format!("/indexes/{name}");
        if self
            .send(Method::GET, &path, None, Some("index_not_found"))
            .await?
            .is_none()
        {
            self.task(
                Method::POST,
                "/indexes",
                json!({"uid":name,"primaryKey":"id"}),
            )
            .await?;
        }
        let fields = if matches!(kind, IndexKind::Admin) {
            json!([
                "publishedTitle",
                "publishedDescription",
                "publishedContent",
                "draftTitle",
                "draftDescription",
                "draftContent"
            ])
        } else {
            json!(["title", "description", "content"])
        };
        let mut s = json!({
            "searchableAttributes": fields,
            "localizedAttributes": [{"attributePatterns": fields, "locales": ["cmn"]}],
            "pagination": {"maxTotalHits": max}
        });
        let (filters, sort, rank) = match kind {
            IndexKind::Public => (
                json!(["deleted", "status"]),
                json!([]),
                json!([
                    "words",
                    "typo",
                    "proximity",
                    "attribute",
                    "exactness",
                    "readCount:desc",
                    "updated:desc"
                ]),
            ),
            IndexKind::Admin => (
                json!([
                    "deleted",
                    "status",
                    "ownerKey",
                    "createdMillis",
                    "hasDraft",
                    "publishedRevisionId"
                ]),
                json!(["createdMillis", "id"]),
                json!([
                    "sort",
                    "words",
                    "typo",
                    "proximity",
                    "attribute",
                    "exactness"
                ]),
            ),
            IndexKind::Revision => (
                json!(["articleKey", "historyKey"]),
                json!(["versionNo"]),
                json!([
                    "sort",
                    "words",
                    "typo",
                    "proximity",
                    "attribute",
                    "exactness"
                ]),
            ),
        };
        s["filterableAttributes"] = filters;
        s["sortableAttributes"] = sort;
        s["rankingRules"] = rank;
        self.task(Method::PATCH, &format!("{path}/settings"), s)
            .await
    }
}
