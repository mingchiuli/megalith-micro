//! JSON contracts shared by the gateway and services at HTTP boundaries.
pub mod auth;
pub mod blog;
pub mod user;

use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize)]
pub struct RpcResult<T> {
    pub code: i32,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub msg: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub data: Option<T>,
}

impl<T> RpcResult<T> {
    pub fn success(data: Option<T>) -> Self {
        Self {
            code: 200,
            msg: Some("success".to_owned()),
            data,
        }
    }
    pub fn fail(code: i32, message: impl Into<String>) -> Self {
        Self {
            code,
            msg: Some(message.into()),
            data: None,
        }
    }
}

#[cfg(feature = "client")]
pub mod client;

pub fn null_default<'de, D, T>(deserializer: D) -> Result<T, D::Error>
where
    D: serde::Deserializer<'de>,
    T: Deserialize<'de> + Default,
{
    Ok(Option::<T>::deserialize(deserializer)?.unwrap_or_default())
}
