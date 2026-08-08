use std::sync::atomic::{AtomicU64, Ordering};

use axum::extract::ws::{Message, WebSocket};
use futures_util::{SinkExt, StreamExt};
use tokio::sync::{broadcast, mpsc, oneshot};
use yrs::{Doc, sync::Awareness};

use super::document_actor::{self, BroadcastPayload, DocCommand};

pub struct BroadcastGroup {
    cmd_tx: mpsc::Sender<DocCommand>,
    broadcast_tx: broadcast::Sender<BroadcastPayload>,
    next_connection_id: AtomicU64,
}

impl BroadcastGroup {
    pub fn new(capacity: usize) -> Self {
        let (cmd_tx, cmd_rx) = mpsc::channel(64);
        let (broadcast_tx, _) = broadcast::channel(capacity);
        let actor_broadcast_tx = broadcast_tx.clone();

        std::thread::spawn(move || {
            let awareness = Awareness::new(Doc::new());
            let runtime = tokio::runtime::Builder::new_current_thread()
                .enable_all()
                .build()
                .expect("Failed to create tokio runtime for document actor");
            runtime.block_on(document_actor::run(awareness, cmd_rx, actor_broadcast_tx));
        });

        Self {
            cmd_tx,
            broadcast_tx,
            next_connection_id: AtomicU64::new(1),
        }
    }

    pub async fn subscribe(
        &self,
        ws: WebSocket,
    ) -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
        let connection_id = self.next_connection_id.fetch_add(1, Ordering::Relaxed);
        let (mut sink, mut stream) = ws.split();

        for message in self.get_initial_messages().await {
            sink.send(Message::Binary(message.into())).await?;
        }

        let mut broadcast_rx = self.broadcast_tx.subscribe();
        loop {
            tokio::select! {
                message = stream.next() => {
                    match message {
                        Some(Ok(Message::Binary(data))) => {
                            for response in self.handle_message(connection_id, &data).await {
                                sink.send(Message::Binary(response.into())).await?;
                            }
                        }
                        Some(Ok(Message::Ping(data))) => sink.send(Message::Pong(data)).await?,
                        Some(Ok(Message::Close(_))) | None => break,
                        Some(Ok(_)) => {}
                        Some(Err(error)) => return Err(error.into()),
                    }
                }
                message = broadcast_rx.recv() => {
                    match message {
                        Ok(payload) if payload.sender_id != connection_id => {
                            sink.send(Message::Binary(payload.data.into())).await?;
                        }
                        Ok(_) => {}
                        Err(broadcast::error::RecvError::Lagged(count)) => {
                            tracing::warn!("Broadcast receiver lagged by {} messages", count);
                        }
                        Err(broadcast::error::RecvError::Closed) => break,
                    }
                }
            }
        }
        Ok(())
    }

    async fn handle_message(&self, sender_id: u64, data: &[u8]) -> Vec<Vec<u8>> {
        let (response_tx, response_rx) = oneshot::channel();
        if self
            .cmd_tx
            .send(DocCommand::HandleMessage {
                sender_id,
                data: data.to_vec(),
                response_tx,
            })
            .await
            .is_err()
        {
            return vec![];
        }
        response_rx.await.unwrap_or_default()
    }

    async fn get_initial_messages(&self) -> Vec<Vec<u8>> {
        let (response_tx, response_rx) = oneshot::channel();
        if self
            .cmd_tx
            .send(DocCommand::GetInitialMessages { response_tx })
            .await
            .is_err()
        {
            return vec![];
        }
        response_rx.await.unwrap_or_default()
    }
}
