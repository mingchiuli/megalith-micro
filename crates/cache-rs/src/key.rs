use std::{collections::HashSet, time::Duration};

use anyhow::{Result, ensure};
use serde::Serialize;
use serde_json::Value;
use sha2::{Digest, Sha256};

#[derive(Debug, Clone, Copy, Hash, PartialEq, Eq)]
pub struct CacheDescriptor {
    pub namespace: &'static str,
    pub version: u32,
}

#[derive(Debug, Clone, Copy)]
pub struct Contract {
    pub descriptor: CacheDescriptor,
    pub ttl: Duration,
    pub track_keys: bool,
}

impl Contract {
    pub const fn new(namespace: &'static str, version: u32, ttl: Duration) -> Self {
        Self {
            descriptor: CacheDescriptor { namespace, version },
            ttl,
            track_keys: false,
        }
    }
}

pub fn validate_contracts(contracts: &[Contract]) -> Result<()> {
    let mut seen = HashSet::new();
    for contract in contracts {
        let descriptor = contract.descriptor;
        let valid = descriptor
            .namespace
            .starts_with(|c: char| c.is_ascii_lowercase())
            && descriptor.namespace.split(['-', '.']).all(|part| {
                !part.is_empty()
                    && part
                        .bytes()
                        .all(|b| b.is_ascii_lowercase() || b.is_ascii_digit())
            });
        ensure!(
            valid && descriptor.version > 0,
            "invalid cache descriptor {descriptor:?}"
        );
        ensure!(!contract.ttl.is_zero(), "zero TTL on {descriptor:?}");
        ensure!(
            seen.insert(descriptor),
            "duplicate cache descriptor {descriptor:?}"
        );
    }
    Ok(())
}

/// The runtime type is part of the Java wire contract, including boxed numeric types.
#[derive(Debug, Serialize)]
pub struct Argument {
    #[serde(rename = "type")]
    java_type: String,
    value: Value,
}

impl Argument {
    pub fn typed(java_type: impl Into<String>, mut value: Value) -> Self {
        value.sort_all_objects();
        Self {
            java_type: java_type.into(),
            value,
        }
    }

    pub fn long(value: i64) -> Self {
        Self::typed("java.lang.Long", value.into())
    }
    pub fn string(value: &str) -> Self {
        Self::typed("java.lang.String", value.into())
    }
    pub fn null() -> Self {
        Self::typed("null", Value::Null)
    }
}

/// Encodes a method argument with the same runtime type name as the Java cache key.
pub trait CacheArgument {
    fn cache_argument(&self) -> Argument;
}

impl CacheArgument for i64 {
    fn cache_argument(&self) -> Argument {
        Argument::long(*self)
    }
}

impl CacheArgument for str {
    fn cache_argument(&self) -> Argument {
        Argument::string(self)
    }
}

impl CacheArgument for String {
    fn cache_argument(&self) -> Argument {
        self.as_str().cache_argument()
    }
}

impl<T: CacheArgument + ?Sized> CacheArgument for &T {
    fn cache_argument(&self) -> Argument {
        T::cache_argument(*self)
    }
}

impl<T: CacheArgument> CacheArgument for Option<T> {
    fn cache_argument(&self) -> Argument {
        self.as_ref()
            .map_or_else(Argument::null, CacheArgument::cache_argument)
    }
}

impl CacheDescriptor {
    pub fn key(self, args: &[Argument]) -> String {
        let bytes = serde_json::to_vec(args).expect("JSON values always serialize");
        format!(
            "{}:v{}:{:x}",
            self.namespace,
            self.version,
            Sha256::digest(bytes)
        )
    }

    pub(crate) fn directory(self) -> String {
        format!(
            "megalith:cache:key-leases:v2:{}:v{}",
            self.namespace, self.version
        )
    }
}
