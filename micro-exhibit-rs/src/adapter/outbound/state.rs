use anyhow::{Context, Result, ensure};
use chrono::{Datelike, Local, Timelike, Weekday};
use megalith_api::{blog::BlogSnapshot, client::BaseClient};
use redis::{AsyncCommands, aio::ConnectionManager};
use serde::Deserialize;
use uuid::Uuid;

const BITMAP: &str = "blog:existence:v1:bitmap";
const READY: &str = "blog:existence:v1:ready";
const LOCK: &str = "blog:existence:v1:rebuild-lock";
const STAGING: &str = "blog:existence:v1:staging";
const VISITS: [&str; 4] = [
    "{visit_record}_day",
    "{visit_record}_week",
    "{visit_record}_month",
    "{visit_record}_year",
];
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Change {
    pub blog_snapshot: BlogSnapshot,
    pub operation: i32,
    pub revision: i64,
    pub previous_published_revision_id: Option<i64>,
}
#[derive(Clone)]
pub struct StateStore {
    pub redis: ConnectionManager,
}

impl StateStore {
    pub async fn rollover(&self) -> Result<()> {
        let now = Local::now();
        if now.hour() != 0 {
            return Ok(());
        }
        let mut redis = self.redis.clone();
        let claim: Option<String> = redis::cmd("SET")
            .arg("statisticsFinishKey")
            .arg(1)
            .arg("NX")
            .arg("EX")
            .arg(23 * 3600)
            .query_async(&mut redis)
            .await?;
        if claim.is_none() {
            return Ok(());
        }
        redis.del::<_, ()>(VISITS[0]).await?;
        if now.weekday() == Weekday::Mon {
            redis.del::<_, ()>(VISITS[1]).await?;
            redis.unlink::<_, ()>("hot_read").await?;
        }
        if now.day() == 1 {
            redis.del::<_, ()>(VISITS[2]).await?;
        }
        if now.ordinal() == 1 {
            redis.del::<_, ()>(VISITS[3]).await?;
        }
        Ok(())
    }
    pub async fn check(&self, id: i64) -> Result<()> {
        let present = redis::cmd("EVAL")
            .arg(include_str!("../../../script/lookup.lua"))
            .arg(2)
            .arg(BITMAP)
            .arg(READY)
            .arg(id)
            .query_async::<i32>(&mut self.redis.clone())
            .await
            .unwrap_or(-1);
        let result = match present {
            0 => "absent",
            1 => "present",
            _ => "fail_open",
        };
        opentelemetry::global::meter("micro-exhibit")
            .u64_counter("megalith.blog.existence.checks")
            .build()
            .add(1, &[opentelemetry::KeyValue::new("result", result)]);
        ensure!(present != 0, "没有找到内容{id} blog");
        Ok(())
    }
    pub async fn apply(&self, change: &Change) -> Result<()> {
        let id = change.blog_snapshot.id.context("missing blog id")?;
        ensure!(
            id > 0 && change.revision >= 0 && (1..=3).contains(&change.operation),
            "invalid blog change"
        );
        loop {
            let done: bool = redis::cmd("EVAL")
                .arg(include_str!("../../../script/event.lua"))
                .arg(6)
                .arg(format!("blog:event-revision:{id}"))
                .arg(BITMAP)
                .arg(READY)
                .arg(format!("read_token:{id}"))
                .arg("hot_read")
                .arg(LOCK)
                .arg(change.revision)
                .arg(change.operation)
                .arg(id)
                .arg(change.blog_snapshot.status.unwrap_or(-1))
                .query_async(&mut self.redis.clone())
                .await?;
            if done {
                return Ok(());
            }
            tokio::time::sleep(std::time::Duration::from_millis(50)).await;
        }
    }
    async fn stage(&self, op: &str, owner: &str, ids: &[i64]) -> Result<bool> {
        Ok(redis::cmd("EVAL")
            .arg(include_str!("../../../script/rebuild.lua"))
            .arg(4)
            .arg(&[LOCK, BITMAP, READY, STAGING])
            .arg(op)
            .arg(owner)
            .arg(ids)
            .query_async(&mut self.redis.clone())
            .await?)
    }
    pub async fn rebuild(&self, base: &BaseClient, batch: i64) -> Result<()> {
        let owner = Uuid::new_v4().to_string();
        if !self.stage("begin", &owner, &[]).await? {
            return Ok(());
        }
        let started = std::time::Instant::now();
        let result = async {
            let mut after = 0;
            loop {
                let ids = base.blog_ids(after, batch).await?;
                if ids.is_empty() {
                    break;
                }
                let last = *ids.last().context("missing last blog id")?;
                ensure!(
                    last > after && ids.iter().all(|id| *id > 0),
                    "invalid blog cursor"
                );
                ensure!(
                    self.stage("batch", &owner, &ids).await?,
                    "rebuild ownership expired"
                );
                after = last;
                if ids.len() < batch as usize {
                    break;
                }
            }
            ensure!(
                self.stage("publish", &owner, &[]).await?,
                "rebuild ownership expired"
            );
            Ok(())
        }
        .await;
        let meter = opentelemetry::global::meter("micro-exhibit");
        meter
            .u64_counter("megalith.blog.existence.rebuilds")
            .build()
            .add(
                1,
                &[opentelemetry::KeyValue::new(
                    "result",
                    if result.is_ok() { "success" } else { "failure" },
                )],
            );
        meter
            .f64_histogram("megalith.blog.existence.rebuild.duration")
            .with_unit("s")
            .build()
            .record(started.elapsed().as_secs_f64(), &[]);
        if result.is_err() {
            let _ = self.stage("abort", &owner, &[]).await;
        }
        result
    }
}

#[async_trait::async_trait]
impl crate::application::ports::ExhibitMetrics for StateStore {
    async fn consume_read_token(&self, id: i64, token: &str) -> Result<bool> {
        Ok(redis::cmd("EVAL")
            .arg(include_str!("../../../script/compare-delete.lua"))
            .arg(1)
            .arg(format!("read_token:{id}"))
            .arg(token)
            .query_async(&mut self.redis.clone())
            .await?)
    }
    async fn visits(&self) -> Result<[i64; 4]> {
        Ok(redis::cmd("EVAL")
            .arg(include_str!("../../../script/multi-pfcount.lua"))
            .arg(4)
            .arg(&VISITS)
            .query_async(&mut self.redis.clone())
            .await?)
    }
}
