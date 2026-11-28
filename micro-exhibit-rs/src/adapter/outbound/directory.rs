use anyhow::{Context, Result};
use cache_rs::{Argument, Cache, Contract, cache};
use megalith_api::{
    blog::{BlogPublicationContentRpcVo, BlogPublicationRpcVo},
    client::{BaseClient, RpcError},
    user::UserEntityRpcVo,
};
use std::{collections::BTreeSet, sync::Arc, time::Duration};

pub const CONTENT: Contract = Contract::new("blog-published-content", 1, Duration::from_secs(300));

#[derive(Clone)]
pub struct BlogDirectory {
    pub base: BaseClient,
    pub cache: Arc<Cache>,
}
impl BlogDirectory {
    #[cache(CONTENT)]
    async fn content(&self, id: i64, revision: i64) -> Result<Option<BlogPublicationContentRpcVo>> {
        Ok(Some(self.base.content(id, revision).await?))
    }
    pub async fn evict_content(
        &self,
        id: i64,
        current: Option<i64>,
        previous: Option<i64>,
    ) -> Result<()> {
        let keys: BTreeSet<_> = [current, previous]
            .into_iter()
            .flatten()
            .map(|r| {
                CONTENT
                    .descriptor
                    .key(&[Argument::long(id), Argument::long(r)])
            })
            .collect();
        self.cache.evict(keys).await
    }
}

#[async_trait::async_trait]
impl crate::application::ports::BlogReader for BlogDirectory {
    async fn increment_views(&self, id: i64) -> Result<()> {
        Ok(self.base.increment_views(id).await?)
    }
    async fn find_by_id(
        &self,
        id: i64,
    ) -> Result<(
        BlogPublicationRpcVo,
        BlogPublicationContentRpcVo,
        UserEntityRpcVo,
    )> {
        let mut publication = self.base.publication(id).await?;
        let content = match self
            .content(id, publication.revision_id.context("missing revision")?)
            .await
        {
            Err(error)
                if error
                    .downcast_ref::<RpcError>()
                    .is_some_and(|e| e.code == 2000) =>
            {
                publication = self.base.publication(id).await?;
                self.content(id, publication.revision_id.context("missing revision")?)
                    .await?
            }
            result => result?,
        }
        .context("missing publication content")?;
        let author = self
            .base
            .user(publication.user_id.context("missing author")?)
            .await?;
        Ok((publication, content, author))
    }
}
