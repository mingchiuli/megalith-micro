use axum::{
    extract::Request,
    http::{request::Builder, HeaderName, HeaderValue},
};
use hyper::HeaderMap;
use std::{collections::HashMap, env};

use crate::{
    exception::error::AuthError,
    util::constant::{
        AUTH_HEADER, AUTH_URL_KEY, CF_CONNECTING_IP, FORWARDED_HEADER, PROXY_CLIENT_IP,
        WL_PROXY_CLIENT_IP,
    },
};

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

pub fn extract_token(req: &Request) -> String {
    req.headers()
        .get("Authorization")
        .and_then(|value| value.to_str().ok())
        .map(String::from)
        .unwrap_or("".to_string())
}

pub fn get_auth_url() -> std::result::Result<hyper::Uri, AuthError> {
    let mut auth_url = env::var(AUTH_URL_KEY)
        .map_err(|_| AuthError::MissingConfig(format!("Missing {}", AUTH_URL_KEY)))?;

    auth_url.push_str("/auth/route");
    Ok(auth_url
        .parse::<hyper::Uri>()
        .map_err(|e| AuthError::InvalidUrl(e.to_string()))?)
}

pub fn set_headers(mut builder: Builder, headers: HashMap<HeaderName, HeaderValue>) -> Builder {
    for (key, value) in headers {
        builder = builder.header(key, value);
    }
    builder
}
