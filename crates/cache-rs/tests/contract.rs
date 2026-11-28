use cache_rs::{Argument, CacheArgument, CacheDescriptor, Contract, validate_contracts};
use serde_json::json;
use std::time::Duration;

#[test]
fn jackson_key_contract() {
    let descriptor = CacheDescriptor {
        namespace: "auth-user-access",
        version: 1,
    };
    assert_eq!(
        descriptor.key(&[Argument::long(42)]),
        "auth-user-access:v1:95397f53ae85e4dbf652b5cf378683c5a574cd812c8de89e5c34884e799dbb41"
    );
    assert_ne!(
        descriptor.key(&[Argument::long(42)]),
        descriptor.key(&[Argument::typed("java.lang.Integer", 42.into())])
    );
    assert_ne!(descriptor.key(&[]), descriptor.key(&[Argument::null()]));
    assert_eq!(
        descriptor.key(&[None::<String>.cache_argument()]),
        descriptor.key(&[Argument::null()])
    );
    assert_eq!(
        descriptor.key(&[Some("admin").cache_argument()]),
        descriptor.key(&[Argument::string("admin")])
    );
    assert_eq!(
        descriptor.key(&[Argument::typed(
            "java.util.LinkedHashMap",
            json!({"a":1,"b":2})
        )]),
        descriptor.key(&[Argument::typed(
            "java.util.LinkedHashMap",
            json!({"b":2,"a":1})
        )])
    );
}

#[test]
fn contracts_require_distinct_valid_names_versions_and_lifetimes() {
    let contract = Contract::new("a.b-c", 1, Duration::from_secs(1));
    assert!(validate_contracts(&[contract]).is_ok());
    assert!(validate_contracts(&[contract, contract]).is_err());
    for namespace in ["", "a..b", "a-", "1a", "A", "a:b"] {
        assert!(validate_contracts(&[Contract::new(namespace, 1, contract.ttl)]).is_err());
    }
    assert!(validate_contracts(&[Contract::new("a", 0, contract.ttl)]).is_err());
    assert!(validate_contracts(&[Contract::new("a", 1, Duration::ZERO)]).is_err());
}
