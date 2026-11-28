use super::{Feed, Recommendation};
use anyhow::Result;
use async_trait::async_trait;
use megalith_api::{
    blog::{BlogPublicationContentRpcVo, BlogPublicationRpcVo},
    user::UserEntityRpcVo,
};

#[async_trait]
pub trait BlogReader: Send + Sync {
    async fn find_by_id(
        &self,
        id: i64,
    ) -> Result<(
        BlogPublicationRpcVo,
        BlogPublicationContentRpcVo,
        UserEntityRpcVo,
    )>;
    async fn increment_views(&self, id: i64) -> Result<()>;
}
#[async_trait]
pub trait ExhibitMetrics: Send + Sync {
    async fn consume_read_token(&self, id: i64, token: &str) -> Result<bool>;
    async fn visits(&self) -> Result<[i64; 4]>;
}
#[async_trait]
pub trait RecommendationReader: Send + Sync {
    async fn feed(&self, cursor: Option<String>) -> Result<Feed<Recommendation>>;
}
