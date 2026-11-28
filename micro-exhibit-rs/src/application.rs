use crate::domain::mask;
use megalith_service_runtime::tasks::BackgroundTasks;
use std::sync::Arc;
pub mod ports;
use anyhow::Result;
use megalith_api::{
    auth::AuthPrincipal,
    blog::{BlogAccessMode, BlogPublicationContentRpcVo, BlogPublicationRpcVo, BlogRenderMode},
    user::UserEntityRpcVo,
};
use megalith_service_runtime::web::WebError;
use ports::{BlogReader, ExhibitMetrics, RecommendationReader};
use serde::{Deserialize, Serialize};

#[derive(Clone)]
pub struct Exhibit {
    pub tasks: BackgroundTasks,
    pub blogs: Arc<dyn BlogReader>,
    pub store: Arc<dyn ExhibitMetrics>,
    pub recommendations: Arc<dyn RecommendationReader>,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Detail {
    #[serde(skip_serializing_if = "Option::is_none")]
    description: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    nickname: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    avatar: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    title: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    content: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    created: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    read_count: Option<i64>,
}
#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Feed<T> {
    pub(crate) content: Vec<T>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) next_cursor: Option<String>,
    pub(crate) has_more: bool,
    pub(crate) restart: bool,
}
#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Recommendation {
    pub(crate) id: i64,
    pub(crate) title: String,
    pub(crate) description: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) link: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) read_count: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) created: Option<String>,
}
pub(crate) fn display_time(value: String) -> String {
    value.split('.').next().unwrap_or(&value).replace('T', " ")
}

impl Exhibit {
    fn increment(&self, id: i64) {
        let base = self.blogs.clone();
        self.tasks.spawn(async move {
            if let Err(error) = base.increment_views(id).await {
                tracing::warn!(%error,"view increment failed");
            }
        });
    }
    pub async fn get_locked_blog(&self, id: i64, token: &str) -> Result<Detail, WebError> {
        let token = token.trim();
        if token.is_empty() || !self.store.consume_read_token(id, token).await? {
            return Err(WebError::token());
        }
        self.increment(id);
        let (publication, content, user) = self.blogs.find_by_id(id).await?;
        Ok(Detail::from_publication(publication, content, user))
    }

    pub async fn get_blog_detail(
        &self,
        id: i64,
        principal: AuthPrincipal,
    ) -> Result<Detail, WebError> {
        let (publication, mut content, user) = self.blogs.find_by_id(id).await?;
        let privileged = principal
            .data_permissions
            .iter()
            .any(|d| d == "BLOG_VIEW_ALL")
            || publication.user_id == Some(principal.user_id as i64);
        if publication.access_mode == Some(BlogAccessMode::Restricted) && !privileged {
            return Err(WebError(401, 10, "认证异常".into()));
        }
        if content.render_mode == Some(BlogRenderMode::SensitiveFilter)
            && !privileged
            && !content.sensitive_content.is_empty()
        {
            for (field, text) in [
                (1, &mut content.title),
                (2, &mut content.description),
                (3, &mut content.content),
            ] {
                if let Some(t) = text {
                    *t = mask(t, &content.sensitive_content, field)?;
                }
            }
        }
        self.increment(id);
        Ok(Detail::from_publication(publication, content, user))
    }
}

impl Detail {
    fn from_publication(
        publication: BlogPublicationRpcVo,
        content: BlogPublicationContentRpcVo,
        user: UserEntityRpcVo,
    ) -> Self {
        Self {
            title: content.title,
            description: content.description,
            content: content.content,
            nickname: user.nickname,
            avatar: user.avatar,
            created: publication
                .published_at
                .or(publication.created)
                .map(display_time),
            read_count: publication.read_count,
        }
    }
}
