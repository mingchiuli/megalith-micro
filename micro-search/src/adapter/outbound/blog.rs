use crate::application::rebuild::Status;
use crate::domain::IndexKind;
use anyhow::{Result, ensure};
use serde_json::Value;

#[derive(Clone)]
pub struct BlogSource {
    pub client: reqwest::Client,
    pub base: String,
}
impl BlogSource {
    pub async fn page(&self, kind: IndexKind, after: i64, limit: usize) -> Result<Value> {
        let (route, cursor) = match kind {
            IndexKind::Public => ("snapshots", "afterId"),
            IndexKind::Admin => ("admin-snapshots", "afterId"),
            IndexKind::Revision => ("revision-snapshots", "afterRevisionId"),
        };
        self.snapshot(&format!("{route}?{cursor}={after}&limit={limit}"))
            .await
    }

    async fn snapshot(&self, suffix: &str) -> Result<Value> {
        let body: Value = self
            .client
            .get(format!("{}/blog/index/{suffix}", self.base))
            .headers(megalith_otel::trace_headers())
            .send()
            .await?
            .error_for_status()?
            .json()
            .await?;
        ensure!(body["code"] == 200, "blog source rejected snapshot read");
        Ok(body["data"].clone())
    }
    pub async fn status(&self) -> Result<Status> {
        Ok(serde_json::from_value(self.snapshot("status").await?)?)
    }
}
