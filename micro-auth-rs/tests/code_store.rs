use anyhow::Result;
use micro_auth_rs::{
    adapter::outbound::codes::CodeStore, application::ports::LoginStore, domain::Channel,
};
use redis::{AsyncCommands, aio::ConnectionManager};
use uuid::Uuid;

#[tokio::test]
#[ignore = "requires an isolated Redis 8"]
async fn reservations_are_single_use_and_cancellation_is_owner_specific() -> Result<()> {
    let redis =
        ConnectionManager::new(redis::Client::open(std::env::var("CACHE_TEST_REDIS_URL")?)?)
            .await?;
    let codes = CodeStore {
        redis: redis.clone(),
        attempts: 3,
        password_window_ms: 900000,
        password_attempts: 3,
    };
    let name = Uuid::new_v4().to_string();
    let (first, second) = tokio::join!(
        codes.reserve(Channel::Email, &name, "aB09x", "first"),
        codes.reserve(Channel::Email, &name, "aB09x", "second")
    );
    let first = first?;
    assert_ne!(first, second?);
    codes
        .cancel(
            Channel::Email,
            &name,
            if first { "second" } else { "first" },
        )
        .await?;
    let key = format!("email_validation:{name}");
    let ttl = redis.clone().pttl::<_, i64>(&key).await?;
    assert!(codes.verify(Channel::Email, &name, "wrong").await.is_err());
    assert!(redis.clone().pttl::<_, i64>(&key).await? <= ttl);
    let (a, b) = tokio::join!(
        codes.verify(Channel::Email, &name, "AB09X"),
        codes.verify(Channel::Email, &name, "AB09X")
    );
    assert_ne!(a.is_ok(), b.is_ok());
    assert!(!redis.clone().exists::<_, bool>(&key).await?);
    assert!(
        codes
            .reserve(Channel::Sms, &name, "123456", "third")
            .await?
    );
    for expected in [
        "sms mismatch",
        "sms mismatch",
        "sms reach max try number",
        "sms not exist",
    ] {
        assert_eq!(
            codes
                .verify(Channel::Sms, &name, "123455")
                .await
                .unwrap_err()
                .to_string(),
            expected
        );
    }
    Ok(())
}
