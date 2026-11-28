use serde::{Deserialize, Serialize};

#[derive(Clone, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogPublicationRpcVo {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub revision_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub user_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub access_mode: Option<BlogAccessMode>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub created: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub published_at: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub read_count: Option<i64>,
}

#[derive(Clone, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogPublicationContentRpcVo {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub title: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub description: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub content: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub render_mode: Option<BlogRenderMode>,
    #[serde(default)]
    pub sensitive_content: Vec<BlogSensitiveSpan>,
}

#[derive(Clone, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogSensitiveSpan {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub start_index: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub end_index: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub r#type: Option<i32>,
}

#[derive(Clone, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogSnapshot {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub user_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub title: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub description: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub content: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub created: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub updated: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub status: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub link: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub read_count: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub revision: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub published_revision_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub published_at: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub offline_at: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub access_mode: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub render_mode: Option<i32>,
    #[serde(default)]
    pub sensitive_content: Vec<BlogSensitiveSpan>,
}

#[derive(Clone, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogDraftSnapshot {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub blog_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub draft_revision: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub base_published_revision_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub title: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub description: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub content: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub link: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub render_mode: Option<BlogRenderMode>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub updated_by: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub updated_at: Option<String>,
    #[serde(default)]
    pub sensitive_content: Vec<BlogSensitiveSpan>,
}

#[derive(Clone, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogRevisionSnapshot {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub blog_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub version_no: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub origin: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub base_revision_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub title: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub description: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub content: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub link: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub render_mode: Option<BlogRenderMode>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub first_published_at: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub created_by: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub created_at: Option<String>,
    #[serde(default)]
    pub sensitive_content: Vec<BlogSensitiveSpan>,
    pub history_generation: i64,
}

#[derive(Clone, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogAdminSnapshot {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub user_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub published_revision_id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub published_at: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub offline_at: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub access_mode: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub publication_revision: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub admin_revision: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub read_count: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub created_at: Option<String>,
    pub history_generation: i64,
}

#[derive(Clone, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogAdminIndexSnapshot {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub blog: Option<BlogAdminSnapshot>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub draft: Option<BlogDraftSnapshot>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub revision: Option<BlogRevisionSnapshot>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub published_revision: Option<BlogRevisionSnapshot>,
}
#[derive(Clone, Debug, Deserialize, Serialize, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct BlogIndexSourceStatus {
    pub read_only: bool,
    pub ready_events: u64,
    pub paused_events: u64,
    pub total: u64,
    pub published_total: u64,
    pub revision_total: u64,
}

impl BlogIndexSourceStatus {
    pub fn ready(&self) -> bool {
        self.read_only && self.ready_events == 0 && self.paused_events == 0
    }
}
#[derive(Clone, Copy, Debug, Deserialize, Serialize, PartialEq, Eq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum BlogAccessMode {
    Public,
    Restricted,
}
#[derive(Clone, Copy, Debug, Deserialize, Serialize, PartialEq, Eq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum BlogRenderMode {
    Normal,
    SensitiveFilter,
}
