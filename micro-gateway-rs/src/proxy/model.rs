use hyper::Method;
use serde::{Deserialize, Serialize};

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub(crate) struct AuthRouteRequest {
    method: String,
    route_mapping: String,
    ip_addr: String,
}

impl AuthRouteRequest {
    pub(crate) fn new(method: &Method, route_mapping: String, ip_addr: String) -> Self {
        Self {
            method: method.to_string(),
            route_mapping,
            ip_addr,
        }
    }
}

#[derive(Clone, Deserialize, Serialize, Debug, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub(crate) struct AuthPrincipal {
    user_id: u64,
    roles: Vec<String>,
    #[serde(default)]
    data_permissions: Vec<String>,
}

#[derive(Clone, Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub(crate) struct AuthorizedRoute {
    service_host: String,
    service_port: u32,
    principal: AuthPrincipal,
}

impl AuthorizedRoute {
    pub(crate) fn service_host(&self) -> &str {
        &self.service_host
    }

    pub(crate) fn service_port(&self) -> u32 {
        self.service_port
    }

    pub(crate) fn principal(&self) -> &AuthPrincipal {
        &self.principal
    }
}

#[cfg(test)]
mod tests {
    use super::*;

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
