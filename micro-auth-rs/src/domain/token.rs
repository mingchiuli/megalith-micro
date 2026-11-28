use anyhow::{Result, ensure};
use base64::{Engine as _, engine::general_purpose::URL_SAFE_NO_PAD};
use jsonwebtoken::{Algorithm, DecodingKey, EncodingKey, Header, Validation, decode, encode};
use serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};
use std::time::{SystemTime, UNIX_EPOCH};
use uuid::Uuid;

#[derive(Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "lowercase")]
pub enum TokenUse {
    Access,
    Refresh,
    Websocket,
}

#[derive(Clone, Serialize, Deserialize)]
#[serde(untagged)]
enum Audience {
    One(String),
    Many(Vec<String>),
}

#[derive(Clone, Serialize, Deserialize)]
pub struct Claims {
    iss: String,
    sub: String,
    aud: Audience,
    iat: u64,
    nbf: u64,
    pub exp: u64,
    jti: String,
    token_use: TokenUse,
    #[serde(skip_serializing_if = "Option::is_none")]
    room_id: Option<String>,
}
impl Claims {
    pub fn user_id(&self) -> Result<i64> {
        Ok(self.sub.parse()?)
    }
}
pub fn now_ms() -> u64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as u64
}
#[derive(Clone)]
pub struct Tokens {
    encoding: EncodingKey,
    key_id: String,
    decoding: DecodingKey,
    issuer: String,
    audience: String,
    pub access_seconds: u64,
    pub refresh_seconds: u64,
    pub websocket_seconds: u64,
}
impl Tokens {
    pub fn new(
        secret: &str,
        issuer: String,
        audience: String,
        access_seconds: u64,
        refresh_seconds: u64,
        websocket_seconds: u64,
    ) -> Result<Self> {
        ensure!(
            secret.encode_utf16().count() >= 64 && !secret.trim().is_empty(),
            "JWT secret must have at least 64 characters"
        );
        ensure!(
            !issuer.trim().is_empty() && !audience.trim().is_empty(),
            "missing JWT issuer or audience"
        );
        ensure!(
            access_seconds > 0 && refresh_seconds > 0 && (1..=60).contains(&websocket_seconds),
            "invalid JWT lifetimes"
        );
        Ok(Self {
            encoding: EncodingKey::from_secret(secret.as_bytes()),
            key_id: URL_SAFE_NO_PAD.encode(Sha256::digest(serde_json::to_vec(
                &serde_json::json!({
                    "k": URL_SAFE_NO_PAD.encode(secret.as_bytes()), "kty": "oct"
                }),
            )?)),
            decoding: DecodingKey::from_secret(secret.as_bytes()),
            issuer,
            audience,
            access_seconds,
            refresh_seconds,
            websocket_seconds,
        })
    }
    pub fn access(&self, id: i64) -> Result<String> {
        self.issue(
            id,
            TokenUse::Access,
            now_ms() / 1000 + self.access_seconds,
            None,
        )
    }
    pub fn refresh(&self, id: i64) -> Result<String> {
        self.issue(
            id,
            TokenUse::Refresh,
            now_ms() / 1000 + self.refresh_seconds,
            None,
        )
    }
    pub fn ticket(&self, id: i64, room: &str, expires_at: u64) -> Result<String> {
        let now = now_ms();
        ensure!(
            expires_at > now && expires_at <= now + 60000,
            "invalid collaboration authorization deadline"
        );
        self.issue(
            id,
            TokenUse::Websocket,
            expires_at / 1000,
            Some(room.to_owned()),
        )
    }
    fn issue(
        &self,
        id: i64,
        token_use: TokenUse,
        exp: u64,
        room_id: Option<String>,
    ) -> Result<String> {
        let now = now_ms() / 1000;
        let claims = Claims {
            iss: self.issuer.clone(),
            sub: id.to_string(),
            aud: Audience::One(self.audience.clone()),
            iat: now,
            nbf: now,
            exp,
            jti: Uuid::new_v4().to_string(),
            token_use,
            room_id,
        };
        let mut header = Header::new(Algorithm::HS512);
        header.kid = Some(self.key_id.clone());
        Ok(encode(&header, &claims, &self.encoding)?)
    }
    pub fn verify(&self, token: &str, usage: TokenUse) -> Result<Claims> {
        let mut rules = Validation::new(Algorithm::HS512);
        rules.set_audience(&[&self.audience]);
        rules.set_issuer(&[&self.issuer]);
        rules.validate_nbf = true;
        rules.leeway = 60;
        let data = decode::<Claims>(token, &self.decoding, &rules)?;
        ensure!(
            data.header.typ.as_deref().is_none_or(
                |v| v.eq_ignore_ascii_case("JWT") || v.eq_ignore_ascii_case("application/jwt")
            ) && data.claims.token_use == usage,
            "invalid token type"
        );
        data.claims.user_id()?;
        Ok(data.claims)
    }
    pub fn route(&self, path: &str, bearer: &str) -> Result<Option<Claims>> {
        if bearer.is_empty() {
            return Ok(None);
        }
        let token = bearer
            .strip_prefix("Bearer ")
            .ok_or_else(|| anyhow::anyhow!("invalid bearer token"))?;
        let Some(room) = path.strip_prefix("/rooms/") else {
            return self.verify(token, TokenUse::Access).map(Some);
        };
        let c = self.verify(token, TokenUse::Websocket)?;
        ensure!(
            now_ms() / 1000 < c.exp
                && c.exp >= c.iat
                && c.exp - c.iat <= 60
                && !room.is_empty()
                && c.room_id.as_deref() == Some(room),
            "invalid WebSocket ticket"
        );
        Ok(Some(c))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    fn service() -> Tokens {
        Tokens::new(
            &"a".repeat(64),
            "micro-auth".into(),
            "megalith-api".into(),
            900,
            604800,
            60,
        )
        .unwrap()
    }
    #[test]
    fn access_refresh_and_room_credentials_are_distinct() {
        let tokens = service();
        let access = tokens.access(42).unwrap();
        assert_eq!(
            tokens
                .verify(&access, TokenUse::Access)
                .unwrap()
                .user_id()
                .unwrap(),
            42
        );
        assert!(tokens.verify(&access, TokenUse::Refresh).is_err());
        assert!(
            tokens
                .verify(&tokens.refresh(42).unwrap(), TokenUse::Access)
                .is_err()
        );
        assert!(tokens.route("/public/blog/stat", "").unwrap().is_none());
        assert!(tokens.route("/public/blog/stat", "Bearer").is_err());
        let ticket = tokens.ticket(42, "room", now_ms() + 20000).unwrap();
        let header = format!("Bearer {ticket}");
        assert!(tokens.route("/rooms/room", &header).unwrap().is_some());
        assert!(tokens.route("/rooms/other", &header).is_err());
        assert!(tokens.route("/public/blog/stat", &header).is_err());
        assert!(tokens.ticket(42, "room", now_ms() + 120000).is_err());
        assert!(tokens.ticket(42, "room", now_ms() - 1).is_err());
    }

    #[test]
    fn websocket_deadline_is_strict_inside_normal_decoder_clock_skew() {
        let tokens = service();
        for exp in [now_ms() / 1000 - 1, now_ms() / 1000 + 120] {
            let raw = tokens
                .issue(42, TokenUse::Websocket, exp, Some("room".into()))
                .unwrap();
            assert!(
                tokens
                    .route("/rooms/room", &format!("Bearer {raw}"))
                    .is_err()
            );
        }
    }
}
