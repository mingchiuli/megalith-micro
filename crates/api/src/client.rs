use crate::{RpcResult, blog::*, user::*};
use reqwest::{Client, Method, RequestBuilder};
use serde::{Deserialize, de::DeserializeOwned};
use std::fmt;

#[derive(Debug)]
pub struct RpcError {
    pub status: u16,
    pub code: i32,
    pub message: String,
}

impl fmt::Display for RpcError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(&self.message)
    }
}
impl std::error::Error for RpcError {}
impl From<reqwest::Error> for RpcError {
    fn from(error: reqwest::Error) -> Self {
        let (status, code) = if error.is_timeout() {
            (504, 9001)
        } else {
            (502, 9000)
        };
        Self {
            status,
            code,
            message: error.without_url().to_string(),
        }
    }
}

#[derive(Deserialize)]
struct ErrorEnvelope {
    code: Option<i32>,
    msg: Option<String>,
}

fn service_error(upstream_status: u16, envelope: Option<ErrorEnvelope>) -> RpcError {
    let ErrorEnvelope { code, msg } = envelope.unwrap_or(ErrorEnvelope {
        code: None,
        msg: None,
    });
    let code = if upstream_status >= 500 {
        9000
    } else {
        code.unwrap_or(9000)
    };
    let (status, code) = match code {
        1 | 22 | 29 | 37 | 39..=41 | 43 | 44 => (404, code),
        8 | 10 | 24 | 25 | 33 | 46 => (401, code),
        31 | 34 | 38 | 51 => (403, code),
        23 | 45 | 47..=50 | 2000 => (409, code),
        19..=21 | 26..=28 | 42 | 1000 | 1001 => (400, code),
        36 => (502, code),
        9001 => (504, code),
        9999 => (500, code),
        _ => (502, 9000),
    };
    let message = if upstream_status >= 400 {
        msg.filter(|value| !value.trim().is_empty())
            .unwrap_or_else(|| format!("downstream service returned HTTP {upstream_status}"))
    } else {
        msg.unwrap_or_else(|| "RemoteServiceException".to_owned())
    };
    RpcError {
        status,
        code,
        message,
    }
}

pub async fn exchange<T: DeserializeOwned>(request: RequestBuilder) -> Result<Option<T>, RpcError> {
    let response = request
        .headers(megalith_otel::trace_headers())
        .send()
        .await?;
    let status = response.status().as_u16();
    if status >= 400 {
        // An error payload can carry data of a different type from a successful response.
        return Err(service_error(
            status,
            response.json::<ErrorEnvelope>().await.ok(),
        ));
    }
    let body = response
        .json::<Option<RpcResult<T>>>()
        .await?
        .ok_or_else(|| RpcError {
            status: 502,
            code: 9000,
            message: "downstream service returned an empty response".into(),
        })?;
    if body.code != 200 {
        return Err(service_error(
            status,
            Some(ErrorEnvelope {
                code: Some(body.code),
                msg: body.msg,
            }),
        ));
    }
    Ok(body.data)
}

pub async fn required<T: DeserializeOwned>(request: RequestBuilder) -> Result<T, RpcError> {
    exchange(request).await?.ok_or_else(|| RpcError {
        status: 502,
        code: 9000,
        message: "downstream service returned an empty response".into(),
    })
}

#[derive(Clone)]
pub struct BaseClient {
    http: Client,
    base: String,
}
impl BaseClient {
    pub fn new(http: Client, base: &str) -> Self {
        Self {
            http,
            base: base.trim_end_matches('/').into(),
        }
    }
    fn request(&self, method: Method, path: &str) -> RequestBuilder {
        self.http.request(method, format!("{}{path}", self.base))
    }
    pub async fn user(&self, id: i64) -> Result<UserEntityRpcVo, RpcError> {
        required(self.request(Method::GET, &format!("/user/{id}"))).await
    }
    pub async fn user_by_login(&self, name: &str) -> Result<UserEntityRpcVo, RpcError> {
        required(
            self.request(Method::GET, "/user/login/query")
                .query(&[("username", name)]),
        )
        .await
    }
    pub async fn user_by_email(&self, email: &str) -> Result<UserEntityRpcVo, RpcError> {
        required(
            self.request(Method::GET, "/user/email")
                .query(&[("email", email)]),
        )
        .await
    }
    pub async fn user_by_phone(&self, phone: &str) -> Result<UserEntityRpcVo, RpcError> {
        required(
            self.request(Method::GET, "/user/phone")
                .query(&[("phone", phone)]),
        )
        .await
    }
    pub async fn user_access(&self, id: i64) -> Result<UserAccessRpcVo, RpcError> {
        required(self.request(Method::GET, &format!("/user/access/{id}"))).await
    }
    pub async fn lock(&self, id: i64) -> Result<(), RpcError> {
        exchange::<()>(self.request(Method::PATCH, &format!("/user/{id}/password-lock")))
            .await
            .map(|_| ())
    }
    pub async fn update_login(&self, name: &str) -> Result<(), RpcError> {
        exchange::<()>(
            self.request(Method::POST, "/user/login/time")
                .query(&[("username", name)]),
        )
        .await
        .map(|_| ())
    }
    pub async fn all_authorities(&self) -> Result<Vec<AuthorityRpcVo>, RpcError> {
        required(self.request(Method::GET, "/authority/list")).await
    }
    pub async fn all_roles(&self) -> Result<Vec<RoleAuthorizationRpcVo>, RpcError> {
        required(self.request(Method::GET, "/role/authorizations")).await
    }
    pub async fn roles(&self, ids: &[i64]) -> Result<Vec<RoleAuthorizationRpcVo>, RpcError> {
        required(self.request(Method::POST, "/role/authorizations").json(ids)).await
    }
    pub async fn navigation(&self, role: &str) -> Result<Vec<MenuRpcVo>, RpcError> {
        required(
            self.request(Method::GET, "/menu/nav")
                .query(&[("role", role)]),
        )
        .await
    }
    pub async fn publication(&self, id: i64) -> Result<BlogPublicationRpcVo, RpcError> {
        required(self.request(Method::GET, &format!("/blog/{id}/publication"))).await
    }
    pub async fn content(
        &self,
        id: i64,
        revision: i64,
    ) -> Result<BlogPublicationContentRpcVo, RpcError> {
        required(self.request(
            Method::GET,
            &format!("/blog/{id}/publication/{revision}/content"),
        ))
        .await
    }
    pub async fn increment_views(&self, id: i64) -> Result<(), RpcError> {
        exchange::<()>(self.request(Method::POST, &format!("/blog/{id}/views")))
            .await
            .map(|_| ())
    }
    pub async fn blog_ids(&self, after: i64, limit: i64) -> Result<Vec<i64>, RpcError> {
        required(
            self.request(Method::GET, "/blog/ids")
                .query(&[("afterId", after), ("limit", limit)]),
        )
        .await
    }
    pub async fn index_status(&self) -> Result<BlogIndexSourceStatus, RpcError> {
        required(self.request(Method::GET, "/blog/index/status")).await
    }
    pub async fn snapshots(&self, after: i64, limit: i64) -> Result<Vec<BlogSnapshot>, RpcError> {
        required(
            self.request(Method::GET, "/blog/index/snapshots")
                .query(&[("afterId", after), ("limit", limit)]),
        )
        .await
    }
    pub async fn admin_snapshots(
        &self,
        after: i64,
        limit: i64,
    ) -> Result<Vec<BlogAdminIndexSnapshot>, RpcError> {
        required(
            self.request(Method::GET, "/blog/index/admin-snapshots")
                .query(&[("afterId", after), ("limit", limit)]),
        )
        .await
    }
    pub async fn revision_snapshots(
        &self,
        after: i64,
        limit: i64,
    ) -> Result<Vec<BlogRevisionSnapshot>, RpcError> {
        required(
            self.request(Method::GET, "/blog/index/revision-snapshots")
                .query(&[("afterRevisionId", after), ("limit", limit)]),
        )
        .await
    }
}

#[derive(Clone)]
pub struct AuthClient {
    http: Client,
    base: String,
}
impl AuthClient {
    pub fn new(http: Client, base: &str) -> Self {
        Self {
            http,
            base: base.trim_end_matches('/').to_owned(),
        }
    }
    pub async fn authorize_route(
        &self,
        request: &crate::auth::AuthRouteRequest,
        authorization: &str,
    ) -> Result<crate::auth::AuthorizedRoute, RpcError> {
        required(
            self.http
                .post(format!("{}{}", self.base, crate::auth::AUTH_ROUTE))
                .header(reqwest::header::AUTHORIZATION, authorization)
                .json(request),
        )
        .await
    }
    pub async fn issue_websocket_ticket(
        &self,
        request: &crate::auth::WebSocketTicketReq,
        principal: Option<&str>,
    ) -> Result<String, RpcError> {
        let mut call = self
            .http
            .post(format!("{}{}", self.base, crate::auth::WEBSOCKET_TOKEN))
            .json(request);
        if let Some(value) = principal {
            call = call.header("X-Megalith-Principal", value);
        }
        required(call).await
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn preserves_known_error_codes_and_normalizes_upstream_failures() {
        for (upstream, input, status, code) in [
            (500, 2000, 502, 9000),
            (404, 2000, 409, 2000),
            (200, 36, 502, 36),
            (403, 999, 502, 9000),
        ] {
            let error = service_error(
                upstream,
                Some(ErrorEnvelope {
                    code: Some(input),
                    msg: Some("detail".into()),
                }),
            );
            assert_eq!(
                (error.status, error.code, error.message.as_str()),
                (status, code, "detail")
            );
        }
        let error = service_error(504, None);
        assert_eq!(error.message, "downstream service returned HTTP 504");
    }
}
