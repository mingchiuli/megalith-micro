use crate::{
    application::{Feed, Recommendation, display_time, ports::RecommendationReader},
    domain::mask,
};
use anyhow::{Result, ensure};
use megalith_api::{
    blog::{BlogRenderMode, BlogSensitiveSpan},
    client::required,
};
use serde::Deserialize;
#[derive(Clone)]
pub struct SearchReader {
    pub http: reqwest::Client,
    pub search: String,
    pub page_size: i64,
}
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct SearchArticle {
    id: i64,
    title: Option<String>,
    description: Option<String>,
    link: Option<String>,
    read_count: Option<i64>,
    created: Option<String>,
    published_at: Option<String>,
    render_mode: Option<BlogRenderMode>,
    #[serde(default, deserialize_with = "megalith_api::null_default")]
    sensitive_content: Vec<BlogSensitiveSpan>,
}
#[async_trait::async_trait]
impl RecommendationReader for SearchReader {
    async fn feed(&self, cursor: Option<String>) -> Result<Feed<Recommendation>> {
        let mut request = self
            .http
            .get(format!(
                "{}/blog/public/recommendations/feed",
                self.search.trim_end_matches('/')
            ))
            .query(&[("size", self.page_size)]);
        if let Some(cursor) = cursor {
            ensure!(cursor.encode_utf16().count() <= 4096, "cursor is too long");
            request = request.query(&[("cursor", cursor)]);
        }
        let source: Feed<SearchArticle> = required(request).await?;
        let mut content = vec![];
        for article in source.content {
            let spans = if article.render_mode == Some(BlogRenderMode::SensitiveFilter) {
                article.sensitive_content
            } else {
                vec![]
            };
            content.push(Recommendation {
                id: article.id,
                title: mask(&article.title.unwrap_or_default(), &spans, 1)?,
                description: mask(&article.description.unwrap_or_default(), &spans, 2)?,
                created: article.published_at.or(article.created).map(display_time),
                link: article.link,
                read_count: article.read_count,
            });
        }
        Ok(Feed {
            content,
            next_cursor: source.next_cursor,
            has_more: source.has_more,
            restart: source.restart,
        })
    }
}
