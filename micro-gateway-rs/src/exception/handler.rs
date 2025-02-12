use hyper::StatusCode;

use super::error::{AuthError, ClientError};

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
            },
            ClientError::Deserialize(e) => {
                log::error!("ClientError::Deserialize:{}", e);
                StatusCode::INTERNAL_SERVER_ERROR
            },
            ClientError::Api(e) => {
                log::error!("ClientError::Api:{}", e);
                StatusCode::INTERNAL_SERVER_ERROR
            }
        }
    } else {
        log::error!("Unknown::err:{}", err);
        StatusCode::INTERNAL_SERVER_ERROR
    }
}