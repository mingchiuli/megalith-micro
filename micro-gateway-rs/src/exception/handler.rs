use axum::{response::IntoResponse, BoxError, Json};
use hyper::StatusCode;
use serde_json::json;

use super::error::{AppError, AuthError, ClientError};


pub async fn handle_error(err: BoxError) -> (StatusCode, String) {
    if let Some(auth_error) = err.downcast_ref::<AuthError>() {
        match auth_error {
            AuthError::MissingConfig(e) => {
                log::error!("AuthError::MissingConfig:{}", e);
                (StatusCode::INTERNAL_SERVER_ERROR, "AuthError::MissingConfig".to_string())
            }
            AuthError::InvalidUrl(e) => {
                log::error!("AuthError::InvalidUrl:{}", e);
                (StatusCode::INTERNAL_SERVER_ERROR, "AuthError::InvalidUrl".to_string())
            }
            AuthError::RequestFailed(e) => {
                log::error!("AuthError::RequestFailed:{}", e);
                (StatusCode::INTERNAL_SERVER_ERROR, "AuthError::RequestFailed".to_string())
            }
            AuthError::Unauthorized(e) => {
                log::error!("AuthError::Unauthorized:{}", e);
                (StatusCode::UNAUTHORIZED, "AuthError::RequestFailed".to_string())
            }
            AuthError::Forbidden(e) => {
                log::error!("AuthError::Forbidden:{}", e);
                (StatusCode::FORBIDDEN, "AuthError::Forbidden".to_string())
            }
        }
    } 
    
    else if let Some(client_error) = err.downcast_ref::<ClientError>() {
        match client_error {
            ClientError::Network(e) => {
                log::error!("ClientError::Network:{}", e);
                (StatusCode::INTERNAL_SERVER_ERROR, "ClientError::Network".to_string())
            }
            ClientError::Serialization(e) => {
                log::error!("ClientError::Serialization:{}", e);
                (StatusCode::INTERNAL_SERVER_ERROR, "ClientError::Serialization".to_string())
            }
            ClientError::Request(e) => {
                log::error!("ClientError::Request:{}", e);
                (StatusCode::BAD_GATEWAY, "ClientError::Request".to_string())
            }
            ClientError::Status(code, e) => {
                log::error!("ClientError::Status:{}", e);
                (StatusCode::from_u16(*code).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR), "ClientError::Status".to_string())
            }
            ClientError::Deserialize(e) => {
                log::error!("ClientError::Deserialize:{}", e);
                (StatusCode::INTERNAL_SERVER_ERROR, "ClientError::Deserialize".to_string())
            }
            ClientError::Api(e) => {
                log::error!("ClientError::Api:{}", e);
                (StatusCode::INTERNAL_SERVER_ERROR, "ClientError::Deserialize".to_string())
            }
        }
    } else {
        log::error!("Unknown::err:{}", err);
        (StatusCode::INTERNAL_SERVER_ERROR, "Unknown::err".to_string())
    }
}

