use anyhow::{Result, ensure};
use redis::{AsyncCommands, aio::ConnectionManager};
use uuid::Uuid;

use crate::{application::ports::LoginStore, domain::Channel};

#[derive(Clone)]
pub struct CodeStore {
    pub redis: ConnectionManager,
    pub attempts: u32,
    pub password_window_ms: u64,
    pub password_attempts: u64,
}
#[async_trait::async_trait]
impl LoginStore for CodeStore {
    async fn reserve(&self, channel: Channel, name: &str, code: &str, id: &str) -> Result<bool> {
        Ok(redis::cmd("EVAL")
            .arg(include_str!("../../../script/reserve-login-code.lua"))
            .arg(1)
            .arg(channel.key(name))
            .arg("code")
            .arg(code)
            .arg("try_count")
            .arg(120)
            .arg(id)
            .query_async(&mut self.redis.clone())
            .await?)
    }
    async fn cancel(&self, channel: Channel, name: &str, id: &str) -> Result<()> {
        redis::cmd("EVAL")
            .arg(include_str!("../../../script/cancel-login-code.lua"))
            .arg(1)
            .arg(channel.key(name))
            .arg(id)
            .query_async::<i64>(&mut self.redis.clone())
            .await?;
        Ok(())
    }
    async fn verify(&self, channel: Channel, name: &str, credential: &str) -> Result<()> {
        let result: String = redis::cmd("EVAL")
            .arg(include_str!("../../../script/verify-login-code.lua"))
            .arg(1)
            .arg(channel.key(name))
            .arg("code")
            .arg("try_count")
            .arg(credential)
            .arg(self.attempts)
            .arg(matches!(channel, Channel::Email) as i32)
            .query_async(&mut self.redis.clone())
            .await?;
        let prefix = if matches!(channel, Channel::Email) {
            "code"
        } else {
            "sms"
        };
        let error = match result.as_str() {
            "ACCEPTED" => return Ok(()),
            "NOT_FOUND" => "not exist",
            "MISMATCH" => "mismatch",
            "EXPIRED" => "expired",
            "ATTEMPTS_EXCEEDED" => "reach max try number",
            _ => anyhow::bail!("invalid code state"),
        };
        anyhow::bail!("{prefix} {error}")
    }
    async fn password_failure(&self, id: i64) -> Result<bool> {
        let now = crate::domain::token::now_ms();
        ensure!(self.password_window_ms > 0, "invalid password window");
        let count: u64 = redis::cmd("EVAL")
            .arg(include_str!("../../../script/password.lua"))
            .arg(1)
            .arg(format!("password_failures:{id}"))
            .arg(now.saturating_sub(self.password_window_ms))
            .arg(now)
            .arg(format!("{now}:{}", Uuid::new_v4()))
            .arg(self.password_window_ms)
            .query_async(&mut self.redis.clone())
            .await?;
        Ok(count >= self.password_attempts)
    }
    async fn clear_failures(&self, id: i64) -> Result<()> {
        Ok(self
            .redis
            .clone()
            .del::<_, ()>(format!("password_failures:{id}"))
            .await?)
    }
    async fn record_visit(&self, ip: &str) -> Result<()> {
        redis::cmd("EVAL")
            .arg(include_str!("../../../script/multi-pfadd.lua"))
            .arg(4)
            .arg(&[
                "{visit_record}_day",
                "{visit_record}_week",
                "{visit_record}_month",
                "{visit_record}_year",
            ])
            .arg(&[ip; 4])
            .query_async::<()>(&mut self.redis.clone())
            .await?;
        Ok(())
    }
}
