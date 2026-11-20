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
        self.wait(r["taskUid"].as_u64().context("missing task UID")?)
            .await
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
                json!(["articleKey", "ownerKey"]),
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
