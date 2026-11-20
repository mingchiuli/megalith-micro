//! Blog snapshots and search projections shared by event delivery and full rebuilds.
use anyhow::{Context, Result, ensure};
use jiff::civil::DateTime;
use serde::Deserialize;
use serde_json::{Map, Value, json};

#[derive(Clone, Copy)]
pub enum IndexKind {
    Public,
    Admin,
    Revision,
}

impl IndexKind {
    pub const ALL: [Self; 3] = [Self::Public, Self::Admin, Self::Revision];

    pub fn snapshot_route(self) -> &'static str {
        match self {
            Self::Public => "snapshots",
            Self::Admin => "admin-snapshots",
            Self::Revision => "revision-snapshots",
        }
    }
}

pub fn number(doc: &Value, key: &str) -> Result<i64> {
    doc[key]
        .as_i64()
        .with_context(|| format!("missing integer field {key}"))
}

pub fn select(doc: &Value, fields: &[&str]) -> Value {
    Value::Object(
        fields
            .iter()
            .filter_map(|key| {
                doc.get(*key)
                    .filter(|v| !v.is_null())
                    .map(|v| ((*key).to_owned(), v.clone()))
            })
            .collect(),
    )
}

pub fn millis(value: &str) -> Result<i64> {
    Ok(value
        .parse::<DateTime>()?
        .in_tz("Asia/Shanghai")?
        .timestamp()
        .as_millisecond())
}

pub fn public_document(blog: &Value, revision: i64, remove: bool) -> Result<Value> {
    let id = number(blog, "id")?;
    if remove {
        return Ok(json!({"id":id,"revision":revision,"deleted":true}));
    }
    let mut doc = select(
        blog,
        &[
            "id",
            "userId",
            "title",
            "description",
            "content",
            "created",
            "updated",
            "status",
            "link",
            "readCount",
            "publishedRevisionId",
            "publishedAt",
            "offlineAt",
            "accessMode",
            "sensitiveContent",
        ],
    );
    doc["deleted"] = false.into();
    doc["revision"] = revision.into();
    doc["renderMode"] = if blog["renderMode"].as_i64() == Some(2) {
        "SENSITIVE_FILTER"
    } else {
        "NORMAL"
    }
    .into();
    if doc["sensitiveContent"].is_null() {
        doc["sensitiveContent"] = json!([]);
    }
    Ok(doc)
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AdminChange {
    #[serde(default)]
    pub operation: String,
    pub blog: Value,
    pub draft: Option<Value>,
    pub revision: Option<Value>,
    pub published_revision: Option<Value>,
}

pub fn revision_document(row: &Value) -> Result<Value> {
    number(row, "id")?;
    let mut doc = select(
        row,
        &[
            "id",
            "blogId",
            "versionNo",
            "origin",
            "baseRevisionId",
            "title",
            "description",
            "content",
            "link",
            "renderMode",
            "firstPublishedAt",
            "createdBy",
            "createdAt",
        ],
    );
    doc["articleKey"] = format!("b{}", number(row, "blogId")?).into();
    doc["ownerKey"] = format!("u{}", number(row, "createdBy")?).into();
    Ok(doc)
}

pub fn admin_document(change: &AdminChange, current: &Value) -> Result<Value> {
    let blog = &change.blog;
    let mut doc = select(
        blog,
        &[
            "id",
            "userId",
            "publicationRevision",
            "adminRevision",
            "readCount",
            "publishedAt",
            "offlineAt",
            "accessMode",
        ],
    );
    number(blog, "id")?;
    number(blog, "adminRevision")?;
    doc["ownerKey"] = format!("u{}", number(blog, "userId")?).into();
    let published = blog["publishedRevisionId"]
        .as_i64()
        .or(current["publishedRevisionId"].as_i64());
    let revision = [change.published_revision.as_ref(), change.revision.as_ref()]
        .into_iter()
        .flatten()
        .find(|r| published.is_some() && r["id"].as_i64() == published);
    doc["publishedRevisionId"] = json!(published);
    doc["created"] = blog["createdAt"].clone();
    if let Some(time) = doc["created"].as_str() {
        doc["createdMillis"] = millis(time)?.into();
    }
    doc["renderMode"] = revision
        .map_or(&current["renderMode"], |r| &r["renderMode"])
        .clone();
    for (field, published_name, draft_name) in [
        ("title", "publishedTitle", "draftTitle"),
        ("description", "publishedDescription", "draftDescription"),
        ("content", "publishedContent", "draftContent"),
        ("link", "publishedLink", "draftLink"),
    ] {
        doc[published_name] = revision
            .map_or(&current[published_name], |r| &r[field])
            .clone();
        if let Some(draft) = &change.draft {
            doc[draft_name] = draft[field].clone();
        }
    }
    doc["hasDraft"] = change.draft.is_some().into();
    doc["draftNewer"] = false.into();
    if let Some(draft) = &change.draft {
        doc["draftRevision"] = draft["draftRevision"].clone();
        doc["basePublishedRevisionId"] = draft["basePublishedRevisionId"].clone();
        doc["draftUpdatedAt"] = draft["updatedAt"].clone();
        if published == draft["basePublishedRevisionId"].as_i64()
            && let (Some(a), Some(b)) = (draft["updatedAt"].as_str(), blog["publishedAt"].as_str())
        {
            doc["draftNewer"] = (a.parse::<DateTime>()? > b.parse::<DateTime>()?).into();
        }
    }
    doc["status"] = if published.is_none() {
        3
    } else if !blog["offlineAt"].is_null() || blog["accessMode"] == 1 {
        1
    } else if doc["renderMode"] == "SENSITIVE_FILTER" {
        2
    } else {
        0
    }
    .into();
    doc["deleted"] = (change.operation == "DELETE").into();
    doc.as_object_mut().unwrap().retain(|_, v| !v.is_null());
    Ok(doc)
}

pub fn public_read(doc: &Value) -> Value {
    let mut result = select(
        doc,
        &[
            "id",
            "userId",
            "publishedRevisionId",
            "versionNo",
            "title",
            "description",
            "content",
            "link",
            "readCount",
            "created",
            "publishedAt",
            "offlineAt",
            "accessMode",
            "renderMode",
            "sensitiveContent",
        ],
    );
    if result["sensitiveContent"].is_null() {
        result["sensitiveContent"] = json!([]);
    }
    result
}

pub fn admin_read(doc: &Value, draft: bool) -> Value {
    let mut result = select(
        doc,
        &[
            "id",
            "userId",
            "publishedRevisionId",
            "publicationRevision",
            "adminRevision",
            "readCount",
            "created",
            "status",
            "accessMode",
            "renderMode",
            "hasDraft",
            "draftNewer",
            "draftRevision",
            "basePublishedRevisionId",
            "draftUpdatedAt",
        ],
    );
    for (to, suffix) in [
        ("title", "Title"),
        ("description", "Description"),
        ("content", "Content"),
        ("link", "Link"),
    ] {
        let key = format!("{}{suffix}", if draft { "draft" } else { "published" });
        if let Some(v) = doc.get(key).filter(|v| !v.is_null()) {
            result[to] = v.clone();
        }
    }
    let updated = if doc["draftNewer"] == true && !doc["draftUpdatedAt"].is_null() {
        &doc["draftUpdatedAt"]
    } else {
        doc.get("publishedAt")
            .filter(|v| !v.is_null())
            .unwrap_or(&doc["created"])
    };
    if !updated.is_null() {
        result["updated"] = updated.clone();
    }
    result
}
pub const HIGHLIGHT: &str = "<b style='color:red'>";
pub fn hit(doc: &Value) -> Result<Value> {
    let mut result = select(
        doc,
        &[
            "id",
            "userId",
            "status",
            "title",
            "description",
            "content",
            "link",
            "created",
        ],
    );
    if let Some(created) = doc["created"].as_str() {
        result["created"] = created
            .parse::<DateTime>()?
            .strftime("%Y-%m-%d %H:%M:%S")
            .to_string()
            .into();
    }
    result["score"] = doc["_rankingScore"].clone();
    let mut fields = Map::new();
    for k in ["title", "description", "content"] {
        if let Some(text) = doc["_formatted"][k]
            .as_str()
            .filter(|s| s.contains(HIGHLIGHT))
        {
            fields.insert(k.into(), json!([text]));
        }
    }
    result["highlight"] = Value::Object(fields);
    Ok(result)
}
pub fn revision_read(doc: &Value, published: Option<i64>) -> Result<Value> {
    let mut result = select(
        doc,
        &[
            "id",
            "blogId",
            "versionNo",
            "origin",
            "baseRevisionId",
            "title",
            "firstPublishedAt",
            "createdBy",
            "createdAt",
        ],
    );
    result["currentPublished"] = (Some(number(doc, "id")?) == published).into();
    Ok(result)
}
pub fn rows(value: &Value) -> Result<&[Value]> {
    Ok(value.as_array().context("expected document array")?)
}
pub fn source_id(row: &Value, kind: IndexKind) -> Result<i64> {
    let id = number(
        if matches!(kind, IndexKind::Admin) {
            &row["blog"]
        } else {
            row
        },
        "id",
    )?;
    ensure!(id > 0, "invalid snapshot id");
    Ok(id)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn tombstones_only_keep_identity_and_revision() {
        let blog = json!({"id":1,"content":"obsolete","status":0});
        assert_eq!(
            public_document(&blog, 4, true).unwrap(),
            json!({"id":1,"revision":4,"deleted":true})
        );
    }

    #[test]
    fn legacy_draft_events_keep_the_published_projection() {
        let old = json!({"publishedRevisionId":3,"publishedTitle":"old","publishedContent":"published","renderMode":"SENSITIVE_FILTER"});
        let change:AdminChange=serde_json::from_value(json!({
            "operation":"DRAFT_UPSERT","blog":{"id":1,"userId":7,"adminRevision":5,"publishedRevisionId":3,"publishedAt":"2026-11-01T00:00:00","createdAt":"1970-01-01T08:00:00"},
            "draft":{"title":"work","draftRevision":2,"basePublishedRevisionId":3,"updatedAt":"2026-11-01T00:00:00.000001"}
        })).unwrap();
        let doc = admin_document(&change, &old).unwrap();
        assert_eq!(doc["status"], 2);
        assert_eq!(doc["ownerKey"], "u7");
        assert_eq!(doc["createdMillis"], 0);
        assert_eq!(doc["draftNewer"], true);
        assert_eq!(admin_read(&doc, false)["content"], "published");
        assert_eq!(admin_read(&doc, true)["title"], "work");
    }

    #[test]
    fn drafts_and_hidden_publications_remain_distinct() {
        let mut event: AdminChange =
            serde_json::from_value(json!({"blog":{"id":7,"userId":42,"adminRevision":1}})).unwrap();
        assert_eq!(admin_document(&event, &Value::Null).unwrap()["status"], 3);
        event.blog["publishedRevisionId"] = 11.into();
        event.blog["accessMode"] = 1.into();
        assert_eq!(admin_document(&event, &Value::Null).unwrap()["status"], 1);
    }
}
