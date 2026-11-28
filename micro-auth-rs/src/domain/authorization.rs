use megalith_api::user::{AuthorityRpcVo, MenuRpcVo};
use serde::{Deserialize, Serialize};
use std::cmp::Ordering;

/// Cache payload owned by auth, also used by the previous Java implementation.
#[derive(Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Authority {
    pub id: Option<i64>,
    pub code: Option<String>,
    pub method_type: Option<String>,
    pub route_pattern: Option<String>,
    pub service_host: Option<String>,
    pub service_port: Option<i32>,
    pub r#type: Option<i32>,
}
impl From<AuthorityRpcVo> for Authority {
    fn from(route: AuthorityRpcVo) -> Self {
        Self {
            id: route.id,
            code: route.code,
            method_type: route.method_type,
            route_pattern: route.route_pattern,
            service_host: route.service_host,
            service_port: route.service_port,
            r#type: route.r#type,
        }
    }
}

fn segments(path: &str) -> Vec<&str> {
    if path.trim().is_empty() {
        Vec::new()
    } else {
        path.strip_prefix('/').unwrap_or(path).split('/').collect()
    }
}
fn specificity(segment: &str) -> u8 {
    match segment {
        "**" => 0,
        "*" => 1,
        _ => 2,
    }
}
fn priority(path: &str) -> (u8, Vec<u8>, usize) {
    let parts: Vec<_> = segments(path).into_iter().map(specificity).collect();
    (
        *parts.iter().min().unwrap_or(&2),
        parts,
        path.encode_utf16().count(),
    )
}
fn matches(pattern: &str, path: &str) -> bool {
    if pattern == path {
        return true;
    }
    let patterns = segments(pattern);
    let parts = segments(path);
    for (index, pattern) in patterns.iter().enumerate() {
        if *pattern == "**" {
            return index + 1 == patterns.len();
        }
        let Some(segment) = parts.get(index) else {
            return false;
        };
        if !(*pattern == "*" && !segment.is_empty() || pattern == segment) {
            return false;
        }
    }
    patterns.len() == parts.len()
}
pub fn route<'a>(routes: &'a [Authority], method: &str, path: &str) -> Option<&'a Authority> {
    routes
        .iter()
        .filter(|r| {
            r.method_type.as_deref() == Some(method)
                && r.route_pattern.as_deref().is_some_and(|p| matches(p, path))
        })
        .min_by(|a, b| {
            let pa = a.route_pattern.as_deref().unwrap_or("");
            let pb = b.route_pattern.as_deref().unwrap_or("");
            let order = priority(pb).cmp(&priority(pa));
            if order != Ordering::Equal {
                return order;
            }
            pa.encode_utf16()
                .cmp(pb.encode_utf16())
                .then_with(|| a.code.cmp(&b.code))
        })
}
#[derive(Serialize)]
pub struct MenuNode {
    #[serde(flatten)]
    menu: MenuRpcVo,
    children: Vec<MenuNode>,
}
pub fn navigation(menus: Vec<MenuRpcVo>) -> Option<MenuNode> {
    let mut enabled = Vec::new();
    for menu in menus {
        if menu.status == Some(0) && !enabled.contains(&menu) {
            enabled.push(menu);
        }
    }
    fn children(menus: &[MenuRpcVo], parent: i64, path: &mut Vec<i64>) -> Vec<MenuNode> {
        let mut nodes = Vec::new();
        for menu in menus.iter().filter(|m| m.parent_id == Some(parent)) {
            let Some(id) = menu.id else { continue };
            if path.contains(&id) {
                continue;
            }
            path.push(id);
            nodes.push(MenuNode {
                menu: menu.clone(),
                children: children(menus, id, path),
            });
            path.pop();
        }
        nodes.sort_by_key(|n| n.menu.order_num.unwrap_or(0));
        nodes
    }
    children(&enabled, 0, &mut vec![0]).into_iter().next()
}

#[cfg(test)]
mod tests {
    use super::*;
    fn authority(pattern: &str) -> Authority {
        Authority {
            id: Some(1),
            code: Some(pattern.into()),
            route_pattern: Some(pattern.into()),
            method_type: Some("GET".into()),
            service_host: Some("micro-base".into()),
            service_port: Some(8082),
            r#type: Some(1),
        }
    }
    #[test]
    fn wildcard_specificity_matches_java_route_ordering() {
        let routes = [
            "/api/**",
            "/api/*/detail",
            "/api/private/*",
            "/api/private/detail",
        ]
        .map(authority);
        assert_eq!(
            route(&routes, "GET", "/api/private/detail")
                .unwrap()
                .route_pattern
                .as_deref(),
            Some("/api/private/detail")
        );
        assert_eq!(
            route(&routes[..3], "GET", "/api/private/detail")
                .unwrap()
                .route_pattern
                .as_deref(),
            Some("/api/private/*")
        );
        assert!(route(&routes, "POST", "/api/private/detail").is_none());
        assert!(route(&routes, "GET", "/apix/public").is_none());
        assert!(matches(
            "/sys/blog/*/versions/*/compare",
            "/sys/blog/42/versions/7/compare"
        ));
        for path in [
            "/sys/blog/42/versions/7",
            "/sys/blog/42/versions/7/history/compare",
            "/sys/blog//versions/7/compare",
        ] {
            assert!(!matches("/sys/blog/*/versions/*/compare", path));
        }
    }
}
