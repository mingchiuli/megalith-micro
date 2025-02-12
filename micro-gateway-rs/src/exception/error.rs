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


/// Client error types
#[derive(Debug)]
pub enum ClientError {
    Network(String),
    Serialization(String),
    Request(String),
    /// Add status code for HTTP errors
    Status(u16, String),
    Deserialize(String),
    Api(String)
}

impl std::error::Error for ClientError {}

impl std::fmt::Display for ClientError {
    fn fmt(&self, f: &mut std::fmt::Formatter) -> std::fmt::Result {
        match self {
            ClientError::Network(msg) => write!(f, "Network error: {}", msg),
            ClientError::Serialization(msg) => write!(f, "Serialization error: {}", msg),
            ClientError::Request(msg) => write!(f, "Request error: {}", msg),
            ClientError::Status(code, msg) => write!(f, "HTTP error {}: {}", code, msg),
            ClientError::Deserialize(msg) => write!(f, "Deserialize error: {}", msg),
            ClientError::Api(msg) => write!(f, "Api error: {}", msg),
        }
    }
}

type GenericError = Box<dyn std::error::Error + Send + Sync>;
pub type Result<T> = std::result::Result<T, GenericError>;