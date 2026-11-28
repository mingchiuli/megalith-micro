//! Workspace implementation of the Megalith read-through cache contract.
mod key;
mod registry;

pub use key::{Argument, CacheArgument, CacheDescriptor, Contract, validate_contracts};
pub use megalith_cache_macros::cache;
pub use registry::{CacheKeyLease, CacheKeyRegistry};
mod cache;
mod config;
mod local;
mod lock;
mod metrics;
mod transport;

pub use cache::Cache;
pub use config::CacheConfig;
pub use transport::{EvictionConfig, RabbitConfig, TransportMode};
