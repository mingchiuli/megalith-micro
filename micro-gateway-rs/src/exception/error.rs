use std::{
    error::Error,
    fmt::{Display, Formatter, Result},
};

use axum::{BoxError, response::{IntoResponse, Response}};
use hyper::StatusCode;

#[derive(Debug)]
pub struct HandlerError {
    status: StatusCode,
    message: String,
}

impl HandlerError {
    /// 创建一个新的 HandlerError 实例
    pub fn new(status: StatusCode, message: impl Into<String>) -> Self {
        Self {
            status,
            message: message.into(),
        }
    }

    /// 获取错误的 HTTP 状态码
    pub fn status(&self) -> StatusCode {
        self.status
    }

    /// 获取错误消息
    pub fn message(&self) -> &str {
        &self.message
    }

    /// 创建一个 FORBIDDEN 错误
    pub fn forbidden(message: &str) -> Self {
        Self::new(StatusCode::FORBIDDEN, message)
    }
}

// 实现 IntoResponse 使其可以直接用作 Axum 响应
impl IntoResponse for HandlerError {
    fn into_response(self) -> Response {
        (self.status, self.message).into_response()
    }
}

// 实现从 ClientError 到 HandlerError 的转换
impl From<ClientError> for HandlerError {
    fn from(error: ClientError) -> Self {
        match error {
            ClientError::Network(msg) => {
                tracing::error!("ClientError::Network:{}", msg);
                HandlerError {
                    status: StatusCode::INTERNAL_SERVER_ERROR,
                    message: msg,
                }
            }
            ClientError::Serialization(msg) => {
                tracing::error!("ClientError::Serialization:{}", msg);
                HandlerError {
                    status: StatusCode::INTERNAL_SERVER_ERROR,
                    message: msg,
                }
            }
            ClientError::Request(msg) => {
                tracing::error!("ClientError::Request:{}", msg);
                HandlerError {
                    status: StatusCode::BAD_GATEWAY,
                    message: msg,
                }
            }
            ClientError::Response(msg) => {
                tracing::error!("ClientError::Response:{}", msg);
                HandlerError {
                    status: StatusCode::INTERNAL_SERVER_ERROR,
                    message: msg,
                }
            }
            ClientError::Status(code, msg) => {
                tracing::error!("ClientError::Status:{}", msg);
                HandlerError {
                    status: StatusCode::from_u16(code).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR),
                    message: msg,
                }
            }
            ClientError::Deserialize(msg) => {
                tracing::error!("ClientError::Deserialize:{}", msg);
                HandlerError {
                    status: StatusCode::INTERNAL_SERVER_ERROR,
                    message: msg,
                }
            }
        }
    }
}

// 实现从 AuthError 到 HandlerError 的转换
impl From<AuthError> for HandlerError {
    fn from(error: AuthError) -> Self {
        match error {
            AuthError::MissingConfig(msg) => {
                tracing::error!("AuthError::MissingConfig:{}", msg);
                HandlerError {
                    status: StatusCode::INTERNAL_SERVER_ERROR,
                    message: msg,
                }
            }
            AuthError::InvalidUrl(msg) => {
                tracing::error!("AuthError::InvalidUrl:{}", msg);
                HandlerError {
                    status: StatusCode::INTERNAL_SERVER_ERROR,
                    message: format!("无效URL: {}", msg),
                }
            }
            AuthError::RequestFailed(msg) => {
                tracing::error!("AuthError::RequestFailed:{}", msg);
                HandlerError {
                    status: StatusCode::INTERNAL_SERVER_ERROR,
                    message: msg,
                }
            }
            AuthError::Unauthorized(msg) => {
                tracing::error!("AuthError::Unauthorized:{}", msg);
                HandlerError {
                    status: StatusCode::UNAUTHORIZED,
                    message: format!("未授权: {}", msg),
                }
            }
        }
    }
}

#[derive(Debug)]
pub enum AuthError {
    RequestFailed(String),
    Unauthorized(String),
    MissingConfig(String),
    InvalidUrl(String),
}

impl Error for AuthError {}

impl Display for AuthError {
    fn fmt(&self, f: &mut Formatter) -> Result {
        match self {
            AuthError::RequestFailed(msg) => write!(f, "RequestFailed error: {}", msg),
            AuthError::Unauthorized(msg) => write!(f, "Unauthorized error: {}", msg),
            AuthError::MissingConfig(msg) => write!(f, "MissingConfig error: {}", msg),
            AuthError::InvalidUrl(msg) => write!(f, "InvalidUrl error: {}", msg),
        }
    }
}

/// Client error types
#[derive(Debug)]
pub enum ClientError {
    Network(String),
    Serialization(String),
    Request(String),
    Response(String),
    /// Add status code for HTTP errors
    Status(u16, String),
    Deserialize(String),
}

impl Error for ClientError {}

impl Display for ClientError {
    fn fmt(&self, f: &mut Formatter) -> Result {
        match self {
            ClientError::Network(msg) => write!(f, "Network error: {}", msg),
            ClientError::Serialization(msg) => write!(f, "Serialization error: {}", msg),
            ClientError::Request(msg) => write!(f, "Request error: {}", msg),
            ClientError::Status(_, msg) => write!(f, "{}", msg),
            ClientError::Deserialize(msg) => write!(f, "Deserialize error: {}", msg),
            ClientError::Response(msg) => write!(f, "Response error: {}", msg),
        }
    }
}

pub fn handle_api_error(e: BoxError) -> ClientError {
    match e.downcast_ref::<ClientError>() {
        Some(ClientError::Status(code, msg)) => ClientError::Status(*code, msg.clone()),
        _ => ClientError::Status(StatusCode::BAD_GATEWAY.as_u16(), e.to_string()),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn client_error_display_messages() {
        assert!(ClientError::Network("net".into()).to_string().contains("Network"));
        assert!(ClientError::Serialization("s".into()).to_string().contains("Serialization"));
        assert!(ClientError::Request("r".into()).to_string().contains("Request"));
        assert!(ClientError::Response("r".into()).to_string().contains("Response"));
        assert!(ClientError::Deserialize("d".into()).to_string().contains("Deserialize"));
        assert_eq!(ClientError::Status(404, "not found".into()).to_string(), "not found");
    }

    #[test]
    fn auth_error_display_messages() {
        assert!(AuthError::Unauthorized("x".into()).to_string().contains("Unauthorized"));
        assert!(AuthError::RequestFailed("x".into()).to_string().contains("RequestFailed"));
        assert!(AuthError::MissingConfig("x".into()).to_string().contains("MissingConfig"));
        assert!(AuthError::InvalidUrl("x".into()).to_string().contains("InvalidUrl"));
    }

    #[test]
    fn client_error_to_handler_error_status_codes() {
        let h: HandlerError = ClientError::Network("e".into()).into();
        assert_eq!(h.status(), StatusCode::INTERNAL_SERVER_ERROR);

        let h: HandlerError = ClientError::Serialization("e".into()).into();
        assert_eq!(h.status(), StatusCode::INTERNAL_SERVER_ERROR);

        let h: HandlerError = ClientError::Request("e".into()).into();
        assert_eq!(h.status(), StatusCode::BAD_GATEWAY);

        let h: HandlerError = ClientError::Response("e".into()).into();
        assert_eq!(h.status(), StatusCode::INTERNAL_SERVER_ERROR);

        let h: HandlerError = ClientError::Status(404, "nf".into()).into();
        assert_eq!(h.status(), StatusCode::NOT_FOUND);
        assert_eq!(h.message(), "nf");

        let h: HandlerError = ClientError::Status(9999, "x".into()).into();
        assert_eq!(h.status(), StatusCode::INTERNAL_SERVER_ERROR);

        let h: HandlerError = ClientError::Deserialize("e".into()).into();
        assert_eq!(h.status(), StatusCode::INTERNAL_SERVER_ERROR);
    }

    #[test]
    fn auth_error_to_handler_error_status_codes() {
        let h: HandlerError = AuthError::Unauthorized("u".into()).into();
        assert_eq!(h.status(), StatusCode::UNAUTHORIZED);

        let h: HandlerError = AuthError::MissingConfig("c".into()).into();
        assert_eq!(h.status(), StatusCode::INTERNAL_SERVER_ERROR);

        let h: HandlerError = AuthError::InvalidUrl("u".into()).into();
        assert_eq!(h.status(), StatusCode::INTERNAL_SERVER_ERROR);

        let h: HandlerError = AuthError::RequestFailed("r".into()).into();
        assert_eq!(h.status(), StatusCode::INTERNAL_SERVER_ERROR);
    }

    #[test]
    fn handle_api_error_preserves_status_when_downcastable() {
        let boxed: BoxError = Box::new(ClientError::Status(403, "no".into()));
        match handle_api_error(boxed) {
            ClientError::Status(c, m) => {
                assert_eq!(c, 403);
                assert_eq!(m, "no");
            }
            _ => panic!("wrong variant"),
        }
    }

    #[test]
    fn handle_api_error_falls_back_to_bad_gateway() {
        let boxed: BoxError = Box::<dyn Error + Send + Sync>::from("plain error");
        match handle_api_error(boxed) {
            ClientError::Status(c, _) => assert_eq!(c, StatusCode::BAD_GATEWAY.as_u16()),
            _ => panic!("wrong variant"),
        }
    }

    #[test]
    fn forbidden_helper_builds_403_error() {
        let e = HandlerError::forbidden("nope");
        assert_eq!(e.status(), StatusCode::FORBIDDEN);
        assert_eq!(e.message(), "nope");
    }
}
