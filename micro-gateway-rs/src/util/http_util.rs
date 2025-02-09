use axum::{
    extract::Request,
    http::{request::Builder, HeaderName, HeaderValue},
};
use hyper::{HeaderMap, StatusCode};
use serde::Deserialize;
use std::{collections::HashMap, env};

use crate::util::constant::{
    AUTH_HEADER, AUTH_URL_KEY, CF_CONNECTING_IP, FORWARDED_HEADER, PROXY_CLIENT_IP,
    WL_PROXY_CLIENT_IP,
};

// Custom error type for auth-related errors
#[derive(Debug)]
pub enum AuthError {
    RequestFailed(String),
    Unauthorized(String),
    Forbidden(String),
    MissingConfig(String),
    InvalidUrl(String),
}

impl std::error::Error for AuthError {}

impl std::fmt::Display for AuthError {
    fn fmt(&self, f: &mut std::fmt::Formatter) -> std::fmt::Result {
        match self {
            AuthError::RequestFailed(msg) => write!(f, "RequestFailed error: {}", msg),
            AuthError::Unauthorized(msg) => write!(f, "Unauthorized error: {}", msg),
            AuthError::Forbidden(msg) => write!(f, "Forbidden error: {}", msg),
            AuthError::MissingConfig(msg) => write!(f, "MissingConfig error: {}", msg),
            AuthError::InvalidUrl(msg) => write!(f, "InvalidUrl error: {}", msg),
        }
    }
}

impl AuthError {
    pub fn msg(self) -> String {
        match self {
            AuthError::RequestFailed(msg) => msg,
            AuthError::Unauthorized(msg) => msg,
            AuthError::Forbidden(msg) => msg,
            AuthError::MissingConfig(msg) => msg,
            AuthError::InvalidUrl(msg) => msg,
        }
    }
}

pub fn status_code_from_error(err: Box<dyn std::error::Error + Send + Sync>) -> StatusCode {
    if let Some(auth_error) = err.downcast_ref::<AuthError>() {
        match auth_error {
            AuthError::MissingConfig(e) => {
                log::error!("AuthError::MissingConfig:{}", e);
                StatusCode::INTERNAL_SERVER_ERROR
            },
            AuthError::InvalidUrl(e) => {
                log::error!("AuthError::InvalidUrl:{}", e);
                StatusCode::INTERNAL_SERVER_ERROR
            },
            AuthError::RequestFailed(e) => {
                log::error!("AuthError::RequestFailed:{}", e);
                StatusCode::INTERNAL_SERVER_ERROR
            },
            AuthError::Unauthorized(e) => {
                log::error!("AuthError::Unauthorized:{}", e);
                StatusCode::UNAUTHORIZED
            },
            AuthError::Forbidden(e) => {
                log::error!("AuthError::Forbidden:{}", e);
                StatusCode::FORBIDDEN
            },
        }
    } else if let Some(client_error) = err.downcast_ref::<ClientError>() {
        match client_error {
            ClientError::Network(e) => {
                log::error!("ClientError::Network:{}", e);
                StatusCode::INTERNAL_SERVER_ERROR
            },
            ClientError::Serialization(e) => {
                log::error!("ClientError::Serialization:{}", e);
                StatusCode::INTERNAL_SERVER_ERROR
            },
            ClientError::Request(e) => {
                log::error!("ClientError::Request:{}", e);
                StatusCode::BAD_GATEWAY
            },
            ClientError::Status(code, e) => {
                log::error!("ClientError::Status:{}", e);
                StatusCode::from_u16(*code).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR)
            }
        }
    } else {
        log::error!("Unknown::err:{}", err);
        StatusCode::INTERNAL_SERVER_ERROR
    }
}

/// Client error types
#[derive(Debug)]
pub enum ClientError {
    Network(String),
    Serialization(String),
    Request(String),
    /// Add status code for HTTP errors
    Status(u16, String),
}

impl std::error::Error for ClientError {}

impl std::fmt::Display for ClientError {
    fn fmt(&self, f: &mut std::fmt::Formatter) -> std::fmt::Result {
        match self {
            ClientError::Network(msg) => write!(f, "Network error: {}", msg),
            ClientError::Serialization(msg) => write!(f, "Serialization error: {}", msg),
            ClientError::Request(msg) => write!(f, "Request error: {}", msg),
            ClientError::Status(code, msg) => write!(f, "HTTP error {}: {}", code, msg),
        }
    }
}

type GenericError = Box<dyn std::error::Error + Send + Sync>;
pub type Result<T> = std::result::Result<T, GenericError>;

#[derive(Deserialize, Debug)]
pub struct ApiResult<T> {
    code: i32,
    msg: String,
    data: T,
}

impl<T> ApiResult<T> {
    /// Creates a new response
    pub fn new(code: i32, msg: String, data: T) -> Self {
        Self { code, msg, data }
    }

    /// Returns a reference to the response data
    pub fn data(&self) -> &T {
        &self.data
    }

    /// Returns the response code
    pub fn code(&self) -> i32 {
        self.code
    }

    /// Returns a reference to the response message
    pub fn msg(&self) -> &str {
        &self.msg
    }

    /// Consumes the response and returns the data
    pub fn into_data(self) -> T {
        self.data
    }

    /// Consumes the response and returns all fields
    pub fn into_inner(self) -> (i32, String, T) {
        (self.code, self.msg, self.data)
    }
}

pub fn get_ip_from_headers(headers: &HeaderMap) -> Option<String> {
    // Check headers in order of preference
    let ip = headers
        .get(CF_CONNECTING_IP) // Cloudflare
        .or_else(|| headers.get(AUTH_HEADER)) // Nginx
        .or_else(|| headers.get(FORWARDED_HEADER)) // General proxy
        .or_else(|| headers.get(PROXY_CLIENT_IP))
        .or_else(|| headers.get(WL_PROXY_CLIENT_IP))
        .and_then(|hv| hv.to_str().ok())
        .and_then(|s| s.split(',').next());

    ip.map(String::from)
}

pub fn extract_token(req: &Request) -> Result<String> {    
    Ok(req.headers()
        .get("Authorization")
        .and_then(|value| value.to_str().ok())
        .map(String::from)
        .unwrap_or("".to_string()))
}

pub fn get_auth_url() -> Result<hyper::Uri> {
    let mut auth_url = env::var(AUTH_URL_KEY)
        .map_err(|_| {
            Box::new(AuthError::MissingConfig(format!(
                "Missing {}",
                AUTH_URL_KEY
            )))
        })?;
    
    auth_url.push_str("/auth/route");
    Ok(auth_url
        .parse::<hyper::Uri>()
        .map_err(|e| Box::new(AuthError::InvalidUrl(e.to_string())))?)
}

pub fn set_headers(
    mut builder: Builder,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Builder> {
    for (key, value) in headers {
        builder = builder.header(key, value);
    }
    Ok(builder)
}
