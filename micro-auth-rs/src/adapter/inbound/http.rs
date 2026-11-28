use crate::{
    application::{Auth, LoginRequest},
    domain::Channel,
    domain::{authorization, token::TokenUse},
};
use anyhow::Result;
use axum::{
    Router,
    body::Bytes,
    extract::State,
    http::{HeaderMap, HeaderValue, header},
    response::{IntoResponse, Response},
    routing::{any, get, post},
};
use megalith_api::auth::{AuthRouteRequest, WebSocketTicketReq};
use megalith_service_runtime::web::{WebError, ok, principal};
use serde::{Deserialize, Serialize, de::DeserializeOwned};
use std::time::{Duration, SystemTime};

pub fn router(auth: Auth) -> Router {
    Router::new()
        .route("/login", post(login))
        .route("/token/refresh", any(refresh))
        .route("/token/logout", post(logout))
        .route("/token/userinfo", get(userinfo))
        .route("/auth/menu/nav", get(nav))
        .route("/code/email", post(email))
        .route("/code/sms", post(sms))
        .route("/inner/auth/route", post(authorize))
        .route("/inner/token/websocket", post(ticket))
        .layer(axum::middleware::map_response(
            |mut res: Response| async move {
                for (name, value) in [
                    ("x-content-type-options", "nosniff"),
                    ("x-xss-protection", "0"),
                    (
                        "cache-control",
                        "no-cache, no-store, max-age=0, must-revalidate",
                    ),
                    ("pragma", "no-cache"),
                    ("expires", "0"),
                    ("x-frame-options", "DENY"),
                ] {
                    res.headers_mut()
                        .insert(name, HeaderValue::from_static(value));
                }
                res
            },
        ))
        .with_state(auth)
}
fn json<T: DeserializeOwned>(body: &Bytes) -> Result<T, WebError> {
    serde_json::from_slice(body)
        .map_err(|_| WebError(400, 1001, "request body is malformed".into()))
}

fn cookie(
    auth: &Auth,
    res: &mut Response,
    name: &str,
    value: &str,
    seconds: u64,
) -> Result<(), WebError> {
    let deadline = if seconds == 0 {
        SystemTime::UNIX_EPOCH
    } else {
        SystemTime::now() + Duration::from_secs(seconds)
    };
    let secure = if auth.cookies.secure { "; Secure" } else { "" };
    let text = format!(
        "{name}={value}; Path={}; Max-Age={seconds}; Expires={}{}; HttpOnly; SameSite={}",
        auth.cookies.path,
        httpdate::fmt_http_date(deadline),
        secure,
        auth.cookies.same_site
    );
    res.headers_mut().append(
        header::SET_COOKIE,
        HeaderValue::try_from(text)
            .map_err(|_| WebError(500, 9999, "invalid cookie configuration".into()))?,
    );
    Ok(())
}
const ACCESS: &str = "megalith_access_token";
const REFRESH: &str = "megalith_refresh_token";
async fn login(
    State(auth): State<Auth>,
    headers: HeaderMap,
    body: Bytes,
) -> Result<Response, WebError> {
    let mime = headers
        .get(header::CONTENT_TYPE)
        .and_then(|v| v.to_str().ok())
        .and_then(|v| v.parse::<mime::Mime>().ok());
    if !mime.is_some_and(|m| m.type_() == mime::APPLICATION && m.subtype() == mime::JSON) {
        return Err(WebError(401, 400, "非法登录".into()));
    }
    let req: LoginRequest =
        serde_json::from_slice(&body).map_err(|_| WebError(401, 400, "非法登录".into()))?;
    let (access, refresh) = auth
        .login(req)
        .await
        .map_err(|e| WebError(401, 400, e.to_string()))?;
    let mut res = ok(None::<()>);
    cookie(&auth, &mut res, ACCESS, &access, auth.tokens.access_seconds)?;
    cookie(
        &auth,
        &mut res,
        REFRESH,
        &refresh,
        auth.tokens.refresh_seconds,
    )?;
    Ok(res)
}
async fn refresh(
    State(auth): State<Auth>,
    method: axum::http::Method,
    uri: axum::http::Uri,
    headers: HeaderMap,
) -> Result<Response, WebError> {
    let value = headers
        .get_all(header::COOKIE)
        .iter()
        .filter_map(|v| v.to_str().ok())
        .flat_map(|v| v.split(';'))
        .map(str::trim)
        .filter_map(|c| c.split_once('='))
        .find(|(name, _)| *name == REFRESH);
    let failure = || {
        (
            axum::http::StatusCode::UNAUTHORIZED,
            [(header::WWW_AUTHENTICATE, "Bearer")],
        )
            .into_response()
    };
    let Some((_, value)) = value else {
        return Ok(failure());
    };
    let Ok(claims) = auth.tokens.verify(value, TokenUse::Refresh) else {
        return Ok(failure());
    };
    if method != axum::http::Method::POST {
        return Ok(megalith_service_runtime::web::not_found(uri).await);
    }
    let id = claims.user_id()?;
    if id == 0 || auth.directory.find_by_id(id).await?.status != Some(0) {
        return Err(WebError::denied());
    }
    let mut res = ok(None::<()>);
    cookie(
        &auth,
        &mut res,
        ACCESS,
        &auth.tokens.access(id)?,
        auth.tokens.access_seconds,
    )?;
    Ok(res)
}
async fn logout(State(auth): State<Auth>) -> Result<Response, WebError> {
    let mut res = ok(None::<()>);
    cookie(&auth, &mut res, ACCESS, "", 0)?;
    cookie(&auth, &mut res, REFRESH, "", 0)?;
    Ok(res)
}
#[derive(Serialize)]
struct UserInfo {
    #[serde(skip_serializing_if = "Option::is_none")]
    id: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    nickname: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    avatar: Option<String>,
}
async fn userinfo(State(auth): State<Auth>, headers: HeaderMap) -> Result<Response, WebError> {
    let user = auth
        .directory
        .find_by_id(principal(&headers)?.user_id as i64)
        .await?;
    Ok(ok(Some(UserInfo {
        id: user.id,
        nickname: user.nickname,
        avatar: user.avatar,
    })))
}
async fn nav(State(auth): State<Auth>, headers: HeaderMap) -> Result<Response, WebError> {
    let mut menus = vec![];
    for role in principal(&headers)?.roles {
        menus.extend(auth.directory.navigation(&role).await?);
    }
    Ok(ok(authorization::navigation(menus)))
}
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct CodeRequest {
    login_name: Option<String>,
}
async fn code(auth: Auth, channel: Channel, body: Bytes) -> Result<Response, WebError> {
    let req: CodeRequest = json(&body)?;
    let name = req
        .login_name
        .filter(|n| !n.trim().is_empty())
        .ok_or_else(|| WebError::validation("loginName must not be blank"))?;
    auth.send_code(channel, &name).await?;
    Ok(ok(None::<()>))
}
async fn email(State(auth): State<Auth>, body: Bytes) -> Result<Response, WebError> {
    code(auth, Channel::Email, body).await
}
async fn sms(State(auth): State<Auth>, body: Bytes) -> Result<Response, WebError> {
    code(auth, Channel::Sms, body).await
}
async fn authorize(
    State(auth): State<Auth>,
    headers: HeaderMap,
    body: Bytes,
) -> Result<Response, WebError> {
    let q: AuthRouteRequest = json(&body)?;
    if q.method.trim().is_empty() {
        return Err(WebError::validation("method must not be blank"));
    }
    if q.route_mapping.trim().is_empty() {
        return Err(WebError::validation("routeMapping must not be blank"));
    }
    let bearer = headers
        .get(header::AUTHORIZATION)
        .and_then(|h| h.to_str().ok())
        .ok_or_else(|| WebError::validation("Missing required header: Authorization"))?;
    Ok(ok(Some(auth.authorize(q, bearer).await?)))
}
async fn ticket(
    State(auth): State<Auth>,
    headers: HeaderMap,
    body: Bytes,
) -> Result<Response, WebError> {
    let q: WebSocketTicketReq = json(&body)?;
    if q.room_id.trim().is_empty() {
        return Err(WebError::validation("roomId must not be blank"));
    }
    let expires_at = q
        .expires_at
        .ok_or_else(|| WebError::validation("expiresAt must not be null"))?;
    let id = principal(&headers)?.user_id;
    if id == 0 {
        return Err(WebError::validation(
            "authenticated internal principal is required",
        ));
    }
    let token = auth
        .tokens
        .ticket(id as i64, &q.room_id, expires_at.try_into().unwrap_or(0))
        .map_err(|e| WebError::validation(&e.to_string()))?;
    Ok(ok(Some(format!("Bearer {token}"))))
}
