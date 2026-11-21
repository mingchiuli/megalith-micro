use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AdminQuery {
    pub page: u32,
    pub page_size: u32,
    pub keywords: Option<String>,
    pub status: Option<i32>,
    pub create_start: Option<String>,
    pub create_end: Option<String>,
    pub user_id: i64,
    pub all_data: bool,
    pub draft_only: Option<bool>,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RevisionQuery {
    pub blog_id: i64,
    pub user_id: i64,
    pub all_data: Option<bool>,
    pub keywords: Option<String>,
    pub page: u32,
    pub page_size: u32,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ReadCount {
    pub blog_id: i64,
    pub read_count: i64,
}

#[derive(Deserialize)]
pub struct FeedQuery {
    pub size: usize,
    pub cursor: Option<String>,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct SearchPage {
    pub total: u64,
    pub current_page: u32,
    pub size: u32,
    pub content: Vec<Value>,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecommendationFeed {
    pub content: Vec<Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub next_cursor: Option<String>,
    pub has_more: bool,
    pub restart: bool,
}
