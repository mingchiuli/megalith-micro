//! Exercises the shipped binary against isolated Meilisearch, Redis and RabbitMQ instances.
use anyhow::{Context, Result, ensure};
use axum::{Json, Router, extract::Query, routing::get};
use lapin::{
    BasicProperties, Connection,
    options::*,
    types::{AMQPValue, FieldTable},
};
use reqwest::Client;
use serde_json::{Value, json};
use std::{
    collections::HashMap,
    env,
    process::{Child, Command, Stdio},
    time::Duration,
};
use tokio::{net::TcpListener, time::sleep};
use uuid::Uuid;

struct Process(Child);
impl Drop for Process {
    fn drop(&mut self) {
        let _ = self.0.kill();
        let _ = self.0.wait();
    }
}
struct Fixture {
    http: Client,
    port: u16,
    meili: String,
    key: String,
    public: String,
    admin: String,
    versions: String,
    blog: String,
    redis: u16,
    rabbit: u16,
    advertised: std::sync::Arc<std::sync::atomic::AtomicU64>,
}
fn env_or(key: &str, default: &str) -> String {
    env::var(key).unwrap_or_else(|_| default.into())
}
impl Fixture {
    async fn new() -> Result<Self> {
        let id = Uuid::new_v4().simple();
        let port = TcpListener::bind("127.0.0.1:0").await?.local_addr()?.port();
        let listener = TcpListener::bind("127.0.0.1:0").await?;
        let blog = format!("http://{}", listener.local_addr()?);
        let advertised = std::sync::Arc::new(std::sync::atomic::AtomicU64::new(1));
        let published = advertised.clone();
        let source = Router::new()
            .route(
                "/blog/index/status",
                get(move || {
                    let count = published.load(std::sync::atomic::Ordering::Relaxed);
                    async move {
                        Json(json!({"code":200, "data":{
                            "readOnly":true, "readyEvents":0, "pausedEvents":0, "total":2,
                            "publishedTotal":count
                        }}))
                    }
                }),
            )
            .route(
                "/blog/index/snapshots",
                get(|Query(q): Query<HashMap<String, String>>| async move {
                    batch(&q, "afterId", vec![blog_row(1, 10, "rebuilt")], false)
                }),
            )
            .route(
                "/blog/index/admin-snapshots",
                get(|Query(q): Query<HashMap<String, String>>| async move {
                    batch(&q, "afterId", vec![admin_row(1), admin_row(2)], true)
                }),
            )
            .route(
                "/blog/index/revision-snapshots",
                get(|Query(q): Query<HashMap<String, String>>| async move {
                    batch(&q, "afterRevisionId", vec![revision()], false)
                }),
            );
        tokio::spawn(async move { axum::serve(listener, source).await.unwrap() });
        Ok(Self {
            http: Client::new(),
            advertised,
            port,
            blog,
            meili: env_or("SEARCH_TEST_MEILI_URL", "http://127.0.0.1:7700"),
            key: env_or("SEARCH_TEST_MEILI_KEY", "integration-test-key"),
            redis: env_or("SEARCH_TEST_REDIS_PORT", "6379").parse()?,
            rabbit: env_or("SEARCH_TEST_RABBIT_PORT", "5672").parse()?,
            public: format!("public-{id}"),
            admin: format!("admin-{id}"),
            versions: format!("revision-{id}"),
        })
    }
    fn url(&self, path: &str) -> String {
        format!("http://127.0.0.1:{}{path}", self.port)
    }
    async fn launch(&self, maintenance: bool) -> Result<Process> {
        let child = Command::new(env!("CARGO_BIN_EXE_micro-search"))
            .env("SERVER_PORT", self.port.to_string())
            .env("SERVER_ADDRESS", "127.0.0.1")
            .env("SPRING_DATA_REDIS_PORT", self.redis.to_string())
            .env("SPRING_DATA_REDIS_PASSWORD", "")
            .env("SPRING_RABBITMQ_PORT", self.rabbit.to_string())
            .env("SPRING_RABBITMQ_VIRTUAL_HOST", "search-test")
            .env("SPRING_RABBITMQ_USERNAME", "integration")
            .env("SPRING_RABBITMQ_PASSWORD", "integration")
            .env("MEGALITH_SEARCH_MEILISEARCH_URL", &self.meili)
            .env("MEGALITH_SEARCH_MEILISEARCH_KEY", &self.key)
            .env("MEGALITH_SEARCH_INDEX_ALIAS", &self.public)
            .env("MEGALITH_SEARCH_INDEX_ADMIN_ALIAS", &self.admin)
            .env("MEGALITH_SEARCH_INDEX_REVISION_ALIAS", &self.versions)
            .env("MEGALITH_BLOG_BLOG_URL", &self.blog)
            .env(
                "MEGALITH_SEARCH_MAINTENANCE_ENABLED",
                maintenance.to_string(),
            )
            .env("MEGALITH_SEARCH_INDEX_REBUILD_BATCH_SIZE", "1")
            .env("RUST_LOG", "warn")
            .stdout(Stdio::null())
            .spawn()?;
        let mut child = Process(child);
        for _ in 0..160 {
            if self
                .http
                .get(self.url("/actuator/health"))
                .send()
                .await
                .is_ok_and(|r| r.status().is_success())
            {
                return Ok(child);
            }
            ensure!(
                child.0.try_wait()?.is_none(),
                "search exited before readiness"
            );
            sleep(Duration::from_millis(100)).await;
        }
        anyhow::bail!("search readiness timeout")
    }
    async fn post(&self, path: &str, body: Value) -> Result<Value> {
        let v: Value = self
            .http
            .post(self.url(path))
            .json(&body)
            .send()
            .await?
            .json()
            .await?;
        ensure!(v["code"] == 200, "{path}: {v}");
        Ok(v["data"].clone())
    }
    async fn await_doc(&self, index: &str, id: i64, field: &str, wanted: Value) -> Result<Value> {
        for _ in 0..200 {
            let v: Value = self
                .http
                .get(format!("{}/indexes/{index}/documents/{id}", self.meili))
                .bearer_auth(&self.key)
                .send()
                .await?
                .json()
                .await?;
            if v[field] == wanted {
                return Ok(v);
            }
            sleep(Duration::from_millis(50)).await;
        }
        anyhow::bail!("{index}/{id} did not reach expected {field}")
    }
}
fn batch(q: &HashMap<String, String>, cursor: &str, rows: Vec<Value>, nested: bool) -> Json<Value> {
    let after = q[cursor].parse::<i64>().unwrap();
    let limit = q["limit"].parse().unwrap();
    let rows = rows
        .into_iter()
        .filter(|v| {
            (if nested { &v["blog"] } else { v })["id"]
                .as_i64()
                .unwrap()
                > after
        })
        .take(limit)
        .collect::<Vec<_>>();
    Json(json!({"code":200,"data":rows}))
}
fn blog_row(id: i64, rev: i64, title: &str) -> Value {
    json!({"id":id,"userId":42,"title":title,"link":"cover","readCount":id,"revision":rev,"status":0,"created":"2026-01-01T08:00:00","publishedAt":"2026-01-01T08:00:00"})
}
fn revision() -> Value {
    json!({"id":11,"blogId":1,"versionNo":1,"title":"published","createdBy":42,"renderMode":"NORMAL"})
}
fn admin_row(id: i64) -> Value {
    let mut data = json!({"blog":{"id":id,"userId":42,"adminRevision":8,"createdAt":"2026-01-01T00:00:00"},"draft":{"title":"草稿","draftRevision":2}});
    if id == 1 {
        data["blog"]["publishedRevisionId"] = 11.into();
        data["publishedRevision"] = revision();
    }
    data
}
async fn publish(ch: &lapin::Channel, admin: bool, body: Value) -> Result<()> {
    let exchange = if admin {
        "blog.admin.change.fanout.exchange"
    } else {
        "blog.change.fanout.exchange"
    };
    let confirm = ch
        .basic_publish(
            exchange.into(),
            "".into(),
            BasicPublishOptions {
                mandatory: true,
                ..Default::default()
            },
            &serde_json::to_vec(&body)?,
            BasicProperties::default(),
        )
        .await?
        .await?;
    ensure!(
        confirm.is_ack() && confirm.take_message().is_none(),
        "event not routed"
    );
    Ok(())
}
#[tokio::test]
#[ignore = "requires isolated RabbitMQ, Redis and Meilisearch"]
async fn contracts_events_cursors_and_rebuild() -> Result<()> {
    let f = Fixture::new().await?;
    let normal = f.launch(false).await?;
    let rabbit = Connection::connect(
        &format!(
            "amqp://integration:integration@127.0.0.1:{}/search-test",
            f.rabbit
        ),
        Default::default(),
    )
    .await?;
    let ch = rabbit.create_channel().await?;
    ch.confirm_select(Default::default()).await?;
    verify_public_search_and_cursor(&f, &ch).await?;
    verify_admin_and_versions(&f, &ch).await?;
    verify_retry_delivery(&ch).await?;
    assert_eq!(
        f.http
            .post(f.url("/inner/search/index/rebuild"))
            .send()
            .await?
            .status(),
        409
    );
    drop(normal);
    let _maintenance = f.launch(true).await?;
    f.advertised.store(3, std::sync::atomic::Ordering::Relaxed);
    assert_eq!(
        f.http
            .post(f.url("/inner/search/index/rebuild"))
            .send()
            .await?
            .status(),
        409
    );
    f.await_doc(&f.public, 1, "deleted", true.into()).await?;
    f.advertised.store(1, std::sync::atomic::Ordering::Relaxed);
    let rebuilt = f.post("/inner/search/index/rebuild", Value::Null).await?;
    let indexes = rebuilt["indexes"]
        .as_array()
        .context("missing index report")?;
    assert_eq!(
        indexes
            .iter()
            .map(|v| v["documents"].as_u64().unwrap())
            .collect::<Vec<_>>(),
        [1, 2, 1]
    );
    f.await_doc(&f.public, 1, "title", "rebuilt".into()).await?;
    let old = indexes[0]["previousIndex"].as_str().unwrap();
    f.await_doc(old, 1, "deleted", true.into()).await?;
    Ok(())
}

async fn verify_public_search_and_cursor(f: &Fixture, ch: &lapin::Channel) -> Result<()> {
    for id in 1..=5 {
        publish(
            ch,
            false,
            json!({"operation":1,"revision":1,"blogSnapshot":blog_row(id,1,"缓存失效")}),
        )
        .await?;
        f.await_doc(&f.public, id, "revision", 1.into()).await?;
    }
    let page: Value = f
        .http
        .get(f.url("/search/public/blog"))
        .query(&[
            ("currentPage", "1"),
            ("keywords", "缓存"),
            ("allInfo", "false"),
        ])
        .send()
        .await?
        .json()
        .await?;
    assert_eq!(page["data"]["totalElements"], 5);
    assert_eq!(page["data"]["last"], true);
    assert_eq!(page["data"]["content"][0]["created"], "2026-01-01 08:00:00");
    let missing: Value = f
        .http
        .get(f.url("/search/public/blog?currentPage=1&keywords=zzzz&allInfo=true"))
        .send()
        .await?
        .json()
        .await?;
    assert_eq!(missing["data"]["totalElements"], 0);
    assert_eq!(missing["data"]["empty"], true);
    assert_eq!(missing["data"]["last"], true);
    assert_eq!(page["data"]["content"][0]["link"], "cover");
    assert!(page["data"]["content"][0]["highlight"]["title"].is_array());
    assert!(
        f.http
            .get(f.url("/search/public/blog?currentPage=1"))
            .send()
            .await?
            .status()
            .is_client_error()
    );
    let feed = "/inner/blog/public/recommendations/feed";
    let first: Value = f
        .http
        .get(f.url(feed))
        .query(&[("size", "2")])
        .send()
        .await?
        .json()
        .await?;
    assert_eq!(first["data"]["content"][0]["id"], 5);
    let cursor = first["data"]["nextCursor"]
        .as_str()
        .context("missing cursor")?;
    f.post(
        "/inner/blog/views/batch",
        json!([{"blogId":1,"readCount":150},{"blogId":1,"readCount":12}]),
    )
    .await?;
    let second: Value = f
        .http
        .get(f.url(feed))
        .query(&[("size", "2"), ("cursor", cursor)])
        .send()
        .await?
        .json()
        .await?;
    assert_eq!(second["data"]["content"][0]["id"], 3);
    assert_eq!(second["data"]["restart"], false);
    let invalid = f
        .http
        .get(f.url(feed))
        .query(&[("size", "2"), ("cursor", "bad")])
        .send()
        .await?;
    assert_eq!(invalid.status(), 400);
    let expired = base64::Engine::encode(
        &base64::engine::general_purpose::URL_SAFE_NO_PAD,
        serde_json::to_vec(&json!({"version":2,"snapshot":Uuid::new_v4(),"offset":2}))?,
    );
    let restarted: Value = f
        .http
        .get(f.url(feed))
        .query(&[("size", "2"), ("cursor", &expired)])
        .send()
        .await?
        .json()
        .await?;
    assert_eq!(restarted["data"]["restart"], true);
    assert_eq!(restarted["data"]["content"][0]["id"], 1);
    let mut row = blog_row(1, 2, "new");
    publish(
        ch,
        false,
        json!({"operation":2,"revision":2,"blogSnapshot":row}),
    )
    .await?;
    let projected = f.await_doc(&f.public, 1, "revision", 2.into()).await?;
    assert_eq!(projected["readCount"], 150);
    publish(
        ch,
        false,
        json!({"operation":3,"revision":3,"blogSnapshot":row}),
    )
    .await?;
    f.await_doc(&f.public, 1, "deleted", true.into()).await?;
    row["title"] = "stale".into();
    publish(
        ch,
        false,
        json!({"operation":2,"revision":2,"blogSnapshot":row}),
    )
    .await?;
    f.post(
        "/inner/blog/views/batch",
        json!([{"blogId":1,"readCount":300}]),
    )
    .await?;
    let removed = f.await_doc(&f.public, 1, "revision", 3.into()).await?;
    assert!(removed.get("title").is_none());
    Ok(())
}

async fn verify_admin_and_versions(f: &Fixture, ch: &lapin::Channel) -> Result<()> {
    publish(ch, true, admin_row(1)).await?;
    f.await_doc(&f.admin, 1, "adminRevision", 8.into()).await?;
    let query = json!({"userId":42,"allData":false,"page":1,"pageSize":5,"draftOnly":true,"keywords":"草稿"});
    assert_eq!(
        f.post("/inner/blog/admin/search", query.clone()).await?["content"][0]["title"],
        "草稿"
    );
    let mut denied = query;
    denied["userId"] = 9.into();
    assert_eq!(
        f.post("/inner/blog/admin/search", denied).await?["total"],
        0
    );
    let versions = f
        .post(
            "/inner/blog/revisions/search",
            json!({"blogId":1,"userId":42,"page":1,"pageSize":10}),
        )
        .await?;
    assert_eq!(versions["content"][0]["currentPublished"], true);
    let restricted = f
        .post(
            "/inner/blog/revisions/search",
            json!({"blogId":1,"userId":41,"page":1,"pageSize":1}),
        )
        .await?;
    assert_eq!(restricted["total"], 0);
    // The admin contract exposes exact page totals above Meilisearch's default cap.
    let documents = (1001..=2005)
        .map(|id| {
            json!({
                "id":id, "publishedRevisionId":id, "ownerKey":"u77", "deleted":false,
                "publishedTitle":"archived", "status":0, "createdMillis":0
            })
        })
        .collect::<Vec<_>>();
    let task: Value = f
        .http
        .post(format!("{}/indexes/{}/documents", f.meili, f.admin))
        .bearer_auth(&f.key)
        .json(&documents)
        .send()
        .await?
        .error_for_status()?
        .json()
        .await?;
    let uid = task["taskUid"]
        .as_u64()
        .context("missing Meilisearch task")?;
    for _ in 0..200 {
        let task: Value = f
            .http
            .get(format!("{}/tasks/{uid}", f.meili))
            .bearer_auth(&f.key)
            .send()
            .await?
            .error_for_status()?
            .json()
            .await?;
        if task["status"] == "succeeded" {
            break;
        }
        ensure!(task["status"] != "failed", "bulk indexing failed");
        sleep(Duration::from_millis(50)).await;
    }
    let last = f
        .post(
            "/inner/blog/admin/search",
            json!({"userId":77,"allData":false,"page":101,"pageSize":10}),
        )
        .await?;
    assert_eq!(last["total"], 1005);
    assert_eq!(last["content"].as_array().unwrap().len(), 5);
    let mut stale = admin_row(1);
    stale["blog"]["adminRevision"] = 7.into();
    stale["draft"]["title"] = "old".into();
    publish(ch, true, stale).await?;
    let doc = f.await_doc(&f.admin, 1, "adminRevision", 8.into()).await?;
    assert_eq!(doc["draftTitle"], "草稿");
    Ok(())
}

async fn verify_retry_delivery(ch: &lapin::Channel) -> Result<()> {
    ch.basic_publish(
        "".into(),
        "blog.change.queue.es".into(),
        Default::default(),
        b"retry-me",
        BasicProperties::default(),
    )
    .await?
    .await?;
    let mut delayed = false;
    for _ in 0..80 {
        if let Some(msg) = ch
            .basic_get("blog.change.queue.es.retry.1".into(), Default::default())
            .await?
        {
            assert_eq!(msg.data, b"retry-me");
            assert_eq!(
                msg.properties
                    .headers()
                    .as_ref()
                    .and_then(|t| t.inner().get("x-megalith-retry-count")),
                Some(&AMQPValue::LongInt(1))
            );
            msg.ack(Default::default()).await?;
            delayed = true;
            break;
        }
        sleep(Duration::from_millis(25)).await;
    }
    assert!(delayed);
    let mut h = FieldTable::default();
    h.insert("x-megalith-retry-count".into(), AMQPValue::LongInt(3));
    ch.basic_publish(
        "".into(),
        "blog.change.queue.es".into(),
        Default::default(),
        b"invalid",
        BasicProperties::default().with_headers(h),
    )
    .await?
    .await?;
    let mut dead = false;
    for _ in 0..100 {
        if let Some(d) = ch
            .basic_get("blog.change.queue.es.dlq".into(), Default::default())
            .await?
        {
            assert_eq!(d.data, b"invalid");
            d.ack(Default::default()).await?;
            dead = true;
            break;
        }
        sleep(Duration::from_millis(50)).await;
    }
    assert!(dead);
    Ok(())
}
