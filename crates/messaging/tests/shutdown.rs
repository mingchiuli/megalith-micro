use anyhow::Result;
use lapin::{
    Connection,
    options::{BasicPublishOptions, QueueDeclareOptions},
    types::FieldTable,
};
use megalith_messaging::{consume, declare_topology};
use std::{sync::Arc, time::Duration};
use tokio::sync::Notify;

#[tokio::test]
#[ignore = "requires a dedicated RabbitMQ vhost"]
async fn finishes_and_acknowledges_an_inflight_delivery_during_shutdown() -> Result<()> {
    const QUEUE: &str = "megalith.interop.shutdown.queue";
    const EXCHANGE: &str = "megalith.interop.shutdown.exchange";
    let url = std::env::var("CACHE_TEST_AMQP_URL")?;
    let connection = Connection::connect(&url, Default::default()).await?;
    let channel = connection.create_channel().await?;
    declare_topology(&channel, QUEUE, EXCHANGE).await?;
    let started = Arc::new(Notify::new());
    let release = Arc::new(Notify::new());
    let worker = consume(url, QUEUE, EXCHANGE, {
        let started = started.clone();
        let release = release.clone();
        move |_| {
            let started = started.clone();
            let release = release.clone();
            async move {
                started.notify_one();
                release.notified().await;
                Ok(())
            }
        }
    });
    channel.confirm_select(Default::default()).await?;
    let confirm = channel
        .basic_publish(
            EXCHANGE.into(),
            "".into(),
            BasicPublishOptions {
                mandatory: true,
                ..Default::default()
            },
            b"fixture",
            Default::default(),
        )
        .await?
        .await?;
    assert!(confirm.is_ack() && confirm.take_message().is_none());
    tokio::time::timeout(Duration::from_secs(5), started.notified()).await?;
    let shutdown = worker.shutdown();
    tokio::pin!(shutdown);
    assert!(
        tokio::time::timeout(Duration::from_millis(50), &mut shutdown)
            .await
            .is_err()
    );
    release.notify_one();
    tokio::time::timeout(Duration::from_secs(5), shutdown).await?;
    // Wait for Rabbit to observe the consumer connection closing before inspecting ready messages.
    tokio::time::timeout(Duration::from_secs(5), async {
        loop {
            let queue = channel
                .queue_declare(
                    QUEUE.into(),
                    QueueDeclareOptions {
                        passive: true,
                        ..Default::default()
                    },
                    FieldTable::default(),
                )
                .await?;
            if queue.consumer_count() == 0 {
                assert_eq!(queue.message_count(), 0);
                return Ok::<_, lapin::Error>(());
            }
            tokio::time::sleep(Duration::from_millis(20)).await;
        }
    })
    .await??;
    Ok(())
}
