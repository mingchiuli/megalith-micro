mod auth_client;
mod http_client;

use auth_client::AuthClient;
pub(crate) use http_client::{HttpClient, request_raw};

#[derive(Clone)]
pub struct GatewayState {
    client: HttpClient,
    auth: AuthClient,
}

impl GatewayState {
    pub fn new() -> Self {
        let client = http_client::create_http_client();
        Self {
            auth: AuthClient::new(client.clone()),
            client,
        }
    }

    pub fn from_config() -> Self {
        let client = http_client::create_http_client();
        Self {
            auth: AuthClient::from_config(client.clone()),
            client,
        }
    }

    pub(crate) fn client(&self) -> &HttpClient {
        &self.client
    }

    pub(crate) fn auth(&self) -> &AuthClient {
        &self.auth
    }
}

impl Default for GatewayState {
    fn default() -> Self {
        Self::new()
    }
}
