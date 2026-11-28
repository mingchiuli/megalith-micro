pub mod ports;

use crate::domain::{
    Channel, authorization,
    token::{Tokens, now_ms},
};
use anyhow::{Context, Result, ensure};
use megalith_api::auth::{AuthPrincipal, AuthRouteRequest, AuthorizedRoute};
use megalith_service_runtime::tasks::BackgroundTasks;
use megalith_service_runtime::web::WebError;
use ports::{Delivery, LoginStore, UserDirectory};
use serde::Deserialize;
use std::sync::Arc;
use uuid::Uuid;

#[derive(Clone)]
pub struct Auth {
    pub tasks: BackgroundTasks,
    pub directory: Arc<dyn UserDirectory>,
    pub tokens: Tokens,
    pub codes: Arc<dyn LoginStore>,
    pub delivery: Arc<dyn Delivery>,
    pub cookies: CookiePolicy,
}

#[derive(Clone)]
pub struct CookiePolicy {
    pub path: String,
    pub secure: bool,
    pub same_site: String,
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LoginRequest {
    login_type: LoginType,
    principal: String,
    credential: String,
}
#[derive(Deserialize, Clone, Copy)]
#[serde(rename_all = "UPPERCASE")]
enum LoginType {
    Password,
    Email,
    Sms,
}

impl Auth {
    pub async fn login(&self, request: LoginRequest) -> Result<(String, String)> {
        let LoginRequest {
            login_type,
            principal,
            credential,
        } = request;
        ensure!(
            !principal.trim().is_empty() && !credential.trim().is_empty(),
            "非法登录"
        );
        let user = self.directory.find_by_login(&principal).await?;
        let id = user.id.context("用户不存在")?;
        let access = self.directory.user_access(id, true).await?;
        let roles = self.directory.roles(&access.role_ids, true).await?;
        ensure!(user.status == Some(0), "账户被锁");
        ensure!(
            roles.iter().any(|r| r.exists
                && r.status == Some(0)
                && r.code.as_deref().is_some_and(|s| !s.trim().is_empty())),
            "role disabled"
        );
        match login_type {
            LoginType::Password => {
                let hash = user.password.unwrap_or_default();
                let valid = tokio::task::spawn_blocking(move || {
                    bcrypt::verify(credential, &hash).unwrap_or(false)
                })
                .await?;
                if !valid {
                    if self.codes.password_failure(id).await? {
                        self.directory.lock_account(id).await?;
                    }
                    anyhow::bail!(
                        "Failed to authenticate since password does not match stored value"
                    );
                }
            }
            LoginType::Email => {
                self.codes
                    .verify(Channel::Email, &principal, &credential)
                    .await?
            }
            LoginType::Sms => {
                self.codes
                    .verify(Channel::Sms, &principal, &credential)
                    .await?
            }
        }
        self.codes.clear_failures(id).await?;
        self.directory.update_login(&principal).await?;
        Ok((self.tokens.access(id)?, self.tokens.refresh(id)?))
    }
    pub async fn send_code(&self, channel: Channel, principal: &str) -> Result<(), WebError> {
        self.directory.require_recipient(channel, principal).await?;
        let code = channel.generate();
        let reservation = Uuid::new_v4().to_string();
        if !self
            .codes
            .reserve(channel, principal, &code, &reservation)
            .await?
        {
            return Err(WebError(409, 23, "code existed".into()));
        }
        let result = match channel {
            Channel::Email => self.delivery.email(principal, &code).await,
            Channel::Sms => self.delivery.sms(principal, &code).await,
        };
        if let Err(failure) = result {
            if let Err(error) = self.codes.cancel(channel, principal, &reservation).await {
                tracing::warn!(%error,"login code cancellation failed");
            }
            return Err(failure.into());
        }
        Ok(())
    }
    pub async fn authorize(
        &self,
        query: AuthRouteRequest,
        bearer: &str,
    ) -> Result<AuthorizedRoute, WebError> {
        let fresh = query.route_mapping == "/sys/blog/edit/ticket";
        let mut expires = fresh.then(|| now_ms() + self.tokens.websocket_seconds * 1000);
        let claims = self
            .tokens
            .route(&query.route_mapping, bearer)
            .map_err(|_| WebError::token())?;
        let id = claims
            .as_ref()
            .map(|c| c.user_id())
            .transpose()
            .map_err(|_| WebError::token())?;
        let routes = self.directory.authorities(fresh).await?;
        let access = match id {
            Some(id) => Some(self.directory.user_access(id, fresh).await?),
            None => None,
        };
        let route = authorization::route(&routes, &query.method, &query.route_mapping)
            .ok_or_else(WebError::denied)?;
        let mut principal = AuthPrincipal {
            user_id: 0,
            roles: vec![],
            data_permissions: vec![],
        };
        if let Some(access) = access {
            if !access.exists || access.status != Some(0) {
                return Err(WebError::denied());
            }
            principal.user_id = access
                .user_id
                .and_then(|i| i.try_into().ok())
                .ok_or_else(WebError::denied)?;
            let mut ids = vec![];
            for id in access.role_ids {
                if !ids.contains(&id) {
                    ids.push(id);
                }
            }
            let roles = self.directory.roles(&ids, fresh).await?;
            let roles = roles
                .iter()
                .filter(|r| r.exists && r.status == Some(0))
                .collect::<Vec<_>>();
            for r in &roles {
                if let Some(code) = &r.code
                    && !principal.roles.contains(code)
                {
                    principal.roles.push(code.clone());
                }
            }
            for permission in [
                "BLOG_VIEW_ALL",
                "BLOG_EDIT_ALL",
                "BLOG_DELETE_ALL",
                "BLOG_EXPORT_ALL",
            ] {
                if roles
                    .iter()
                    .any(|r| r.data_permissions.iter().any(|p| p == permission))
                {
                    principal.data_permissions.push(permission.to_owned());
                }
            }
            if !query.route_mapping.starts_with("/rooms/")
                && route.r#type != Some(0)
                && !roles.iter().any(|r| {
                    route
                        .code
                        .as_ref()
                        .is_some_and(|c| r.authority_codes.contains(c))
                })
            {
                return Err(WebError::denied());
            }
        } else if route.r#type != Some(0) {
            return Err(WebError::token());
        }
        if !query.ip_addr.is_empty() {
            let codes = self.codes.clone();
            self.tasks.spawn(async move {
                if let Err(error) = codes.record_visit(&query.ip_addr).await {
                    tracing::warn!(%error,"visit recording failed");
                }
            });
        }
        if query.route_mapping.starts_with("/rooms/") {
            expires = Some(claims.ok_or_else(WebError::token)?.exp * 1000);
        }
        Ok(AuthorizedRoute {
            service_host: route.service_host.clone().context("missing service host")?,
            service_port: route
                .service_port
                .and_then(|p| p.try_into().ok())
                .context("invalid service port")?,
            principal,
            authorization_expires_at: expires,
            route_pattern: route.route_pattern.clone(),
        })
    }
}
