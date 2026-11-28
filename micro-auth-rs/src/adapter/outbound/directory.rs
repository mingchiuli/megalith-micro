use crate::domain::authorization::Authority;
use anyhow::{Context, Result};
use cache_rs::{Argument, Cache, Contract, cache};
use megalith_api::{client::BaseClient, user::*};
use serde::Deserialize;
use std::{collections::BTreeSet, sync::Arc, time::Duration};

pub const USER_ACCESS: Contract = Contract::new("auth-user-access", 1, Duration::from_secs(1800));
pub const ROLE_AUTHORIZATION: Contract =
    Contract::new("auth-role-authorization", 1, Duration::from_secs(1800));
pub const ROLE_NAVIGATION: Contract =
    Contract::new("auth-role-navigation", 1, Duration::from_secs(1800));
pub const AUTHORITIES: Contract =
    Contract::new("auth-system-authorities", 1, Duration::from_secs(1800));
pub const CONTRACTS: [Contract; 4] = [
    USER_ACCESS,
    ROLE_AUTHORIZATION,
    ROLE_NAVIGATION,
    AUTHORITIES,
];

#[derive(Clone)]
pub struct Directory {
    pub base: BaseClient,
    pub cache: Arc<Cache>,
}

#[async_trait::async_trait]
impl crate::application::ports::UserDirectory for Directory {
    async fn find_by_id(&self, id: i64) -> Result<UserEntityRpcVo> {
        Ok(self.base.user(id).await?)
    }
    async fn find_by_login(&self, name: &str) -> Result<UserEntityRpcVo> {
        Ok(self.base.user_by_login(name).await?)
    }
    async fn require_recipient(&self, channel: crate::domain::Channel, name: &str) -> Result<()> {
        match channel {
            crate::domain::Channel::Email => self.base.user_by_email(name).await?,
            crate::domain::Channel::Sms => self.base.user_by_phone(name).await?,
        };
        Ok(())
    }
    async fn lock_account(&self, id: i64) -> Result<()> {
        Ok(self.base.lock(id).await?)
    }
    async fn update_login(&self, name: &str) -> Result<()> {
        Ok(self.base.update_login(name).await?)
    }

    async fn user_access(&self, id: i64, fresh: bool) -> Result<UserAccessRpcVo> {
        if fresh {
            return Ok(self.base.user_access(id).await?);
        }
        self.cached_user_access(id)
            .await?
            .context("missing user access")
    }
    async fn authorities(&self, fresh: bool) -> Result<Vec<Authority>> {
        if fresh {
            return self.load_authorities().await;
        }
        self.cached_authorities()
            .await?
            .context("missing authorities")
    }
    async fn roles(&self, ids: &[i64], fresh: bool) -> Result<Vec<RoleAuthorizationRpcVo>> {
        if ids.is_empty() {
            return Ok(Vec::new());
        }
        if fresh {
            return Ok(self.base.roles(ids).await?);
        }
        let roles = self
            .cached_role_authorizations()
            .await?
            .context("missing role authorizations")?;
        Ok(ids
            .iter()
            .map(|id| {
                roles
                    .iter()
                    .find(|r| r.role_id == Some(*id))
                    .cloned()
                    .unwrap_or_else(|| RoleAuthorizationRpcVo::missing(*id))
            })
            .collect())
    }
    async fn navigation(&self, role: &str) -> Result<Vec<MenuRpcVo>> {
        self.cached_navigation(role)
            .await?
            .context("missing navigation")
    }
}

impl Directory {
    #[cache(USER_ACCESS)]
    async fn cached_user_access(&self, id: i64) -> Result<Option<UserAccessRpcVo>> {
        Ok(Some(self.base.user_access(id).await?))
    }

    #[cache(ROLE_AUTHORIZATION)]
    async fn cached_role_authorizations(&self) -> Result<Option<Vec<RoleAuthorizationRpcVo>>> {
        Ok(Some(self.base.all_roles().await?))
    }

    #[cache(ROLE_NAVIGATION)]
    async fn cached_navigation(&self, role: &str) -> Result<Option<Vec<MenuRpcVo>>> {
        Ok(Some(self.base.navigation(role).await?))
    }

    #[cache(AUTHORITIES)]
    async fn cached_authorities(&self) -> Result<Option<Vec<Authority>>> {
        Ok(Some(self.load_authorities().await?))
    }

    async fn load_authorities(&self) -> Result<Vec<Authority>> {
        Ok(self
            .base
            .all_authorities()
            .await?
            .into_iter()
            .map(Authority::from)
            .collect())
    }
    pub async fn evict(&self, event: &AuthCacheEvictMessage) -> Result<()> {
        self.cache.evict(event.keys()).await
    }
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthCacheEvictMessage {
    #[serde(default, deserialize_with = "megalith_api::null_default")]
    pub user_ids: Vec<i64>,
    #[serde(default, deserialize_with = "megalith_api::null_default")]
    pub role_ids: Vec<i64>,
    #[serde(default, deserialize_with = "megalith_api::null_default")]
    pub role_codes: Vec<String>,
    pub evict_menus: bool,
    pub evict_routes: bool,
}
impl AuthCacheEvictMessage {
    pub fn keys(&self) -> BTreeSet<String> {
        let mut keys: BTreeSet<_> = self
            .user_ids
            .iter()
            .map(|id| USER_ACCESS.descriptor.key(&[Argument::long(*id)]))
            .collect();
        if !self.role_ids.is_empty() {
            keys.insert(ROLE_AUTHORIZATION.descriptor.key(&[]));
        }
        if self.evict_menus {
            keys.extend(
                self.role_codes
                    .iter()
                    .map(|code| ROLE_NAVIGATION.descriptor.key(&[Argument::string(code)])),
            );
        }
        if self.evict_routes {
            keys.insert(AUTHORITIES.descriptor.key(&[]));
        }
        keys
    }
}
