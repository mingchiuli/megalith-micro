use crate::domain::{Channel, authorization::Authority};
use anyhow::Result;
use async_trait::async_trait;
use megalith_api::user::{MenuRpcVo, RoleAuthorizationRpcVo, UserAccessRpcVo, UserEntityRpcVo};

#[async_trait]
pub trait UserDirectory: Send + Sync {
    async fn find_by_id(&self, id: i64) -> Result<UserEntityRpcVo>;
    async fn find_by_login(&self, name: &str) -> Result<UserEntityRpcVo>;
    async fn require_recipient(&self, channel: Channel, name: &str) -> Result<()>;
    async fn lock_account(&self, id: i64) -> Result<()>;
    async fn update_login(&self, name: &str) -> Result<()>;
    async fn user_access(&self, id: i64, fresh: bool) -> Result<UserAccessRpcVo>;
    async fn authorities(&self, fresh: bool) -> Result<Vec<Authority>>;
    async fn roles(&self, ids: &[i64], fresh: bool) -> Result<Vec<RoleAuthorizationRpcVo>>;
    async fn navigation(&self, role: &str) -> Result<Vec<MenuRpcVo>>;
}

#[async_trait]
pub trait LoginStore: Send + Sync {
    async fn reserve(
        &self,
        channel: Channel,
        name: &str,
        code: &str,
        reservation: &str,
    ) -> Result<bool>;
    async fn cancel(&self, channel: Channel, name: &str, reservation: &str) -> Result<()>;
    async fn verify(&self, channel: Channel, name: &str, credential: &str) -> Result<()>;
    async fn password_failure(&self, id: i64) -> Result<bool>;
    async fn clear_failures(&self, id: i64) -> Result<()>;
    async fn record_visit(&self, ip: &str) -> Result<()>;
}

#[async_trait]
pub trait Delivery: Send + Sync {
    async fn email(&self, to: &str, code: &str) -> Result<()>;
    async fn sms(&self, phone: &str, code: &str) -> Result<()>;
}
