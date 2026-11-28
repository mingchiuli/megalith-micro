use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthRouteRequest {
    #[serde(default, deserialize_with = "crate::null_default")]
    pub method: String,
    #[serde(default, deserialize_with = "crate::null_default")]
    pub route_mapping: String,
    #[serde(default, deserialize_with = "crate::null_default")]
    pub ip_addr: String,
}

impl AuthRouteRequest {
    pub fn new(method: impl ToString, route_mapping: String, ip_addr: String) -> Self {
        Self {
            method: method.to_string(),
            route_mapping,
            ip_addr,
        }
    }
}

#[derive(Clone, Deserialize, Serialize, Debug, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct AuthPrincipal {
    pub user_id: u64,
    pub roles: Vec<String>,
    #[serde(default, deserialize_with = "crate::null_default")]
    pub data_permissions: Vec<String>,
}

#[derive(Clone, Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct AuthorizedRoute {
    pub service_host: String,
    pub service_port: u32,
    pub principal: AuthPrincipal,
    #[serde(default, skip_serializing_if = "Option::is_none")]
    pub authorization_expires_at: Option<u64>,
    #[serde(default, skip_serializing_if = "Option::is_none")]
    pub route_pattern: Option<String>,
}

impl AuthorizedRoute {
    pub fn route_pattern(&self) -> &str {
        self.route_pattern
            .as_deref()
            .filter(|value| !value.is_empty())
            .unwrap_or("unknown")
    }

    pub fn authorization_expires_at(&self) -> Option<u64> {
        self.authorization_expires_at
    }

    pub fn service_host(&self) -> &str {
        &self.service_host
    }

    pub fn service_port(&self) -> u32 {
        self.service_port
    }

    pub fn principal(&self) -> &AuthPrincipal {
        &self.principal
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn route_metrics_use_the_authorized_template_and_legacy_responses_are_bounded() {
        let legacy: AuthorizedRoute = serde_json::from_value(serde_json::json!({
            "serviceHost": "blog", "servicePort": 8082,
            "principal": {"userId": 42, "roles": []}
        }))
        .unwrap();
        assert_eq!(legacy.route_pattern(), "unknown");
        for _ in [7, 42, 999] {
            let route: AuthorizedRoute = serde_json::from_value(serde_json::json!({
                "serviceHost": "sync", "servicePort": 8089, "routePattern": "/rooms/*",
                "principal": {"userId": 42, "roles": []}
            }))
            .unwrap();
            assert_eq!(route.route_pattern(), "/rooms/*");
        }
    }

    #[test]
    fn principal_serializes_as_the_java_contract() {
        let principal: AuthPrincipal = serde_json::from_value(serde_json::json!({
            "userId": 42,
            "roles": ["user"],
            "dataPermissions": ["BLOG_VIEW_ALL"]
        }))
        .unwrap();

        assert_eq!(
            serde_json::to_string(&principal).unwrap(),
            r#"{"userId":42,"roles":["user"],"dataPermissions":["BLOG_VIEW_ALL"]}"#
        );
    }
}

pub const AUTH_ROUTE: &str = "/auth/route";
pub const WEBSOCKET_TOKEN: &str = "/token/websocket";

#[derive(Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct WebSocketTicketReq {
    #[serde(default, deserialize_with = "crate::null_default")]
    pub room_id: String,
    pub expires_at: Option<i64>,
}
