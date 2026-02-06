use tokio::sync::{broadcast, mpsc, oneshot};
use yrs::sync::{Awareness, AwarenessUpdate, Message, SyncMessage};
use yrs::updates::decoder::Decode;
use yrs::updates::encoder::Encode;
use yrs::{Doc, ReadTxn, Transact};

/// Message types following y-websocket protocol
pub const MSG_SYNC: u8 = 0;
pub const MSG_AWARENESS: u8 = 1;

/// Commands sent to the document actor
enum DocCommand {
    HandleMessage {
        data: Vec<u8>,
        response_tx: oneshot::Sender<Option<Vec<u8>>>,
    },
    CreateSyncStep1 {
        response_tx: oneshot::Sender<Vec<u8>>,
    },
    CreateAwarenessUpdate {
        response_tx: oneshot::Sender<Option<Vec<u8>>>,
    },
}

/// BroadcastGroup manages document synchronization and awareness for a room
/// Uses an actor pattern to handle non-Send Awareness type
pub struct BroadcastGroup {
    cmd_tx: mpsc::Sender<DocCommand>,
    broadcast_tx: broadcast::Sender<Vec<u8>>,
}

impl BroadcastGroup {
    pub fn new(capacity: usize) -> Self {
        let (cmd_tx, cmd_rx) = mpsc::channel(32);
        let (broadcast_tx, _) = broadcast::channel(capacity);

        // Spawn the document actor on a dedicated thread
        // Create Doc and Awareness inside the thread to avoid Send issues
        let broadcast_tx_clone = broadcast_tx.clone();
        std::thread::spawn(move || {
            let doc = Doc::new();
            let awareness = Awareness::new(doc);

            let rt = tokio::runtime::Builder::new_current_thread()
                .enable_all()
                .build()
                .unwrap();
            rt.block_on(doc_actor(awareness, cmd_rx, broadcast_tx_clone));
        });

        BroadcastGroup {
            cmd_tx,
            broadcast_tx,
        }
    }

    pub fn subscribe(&self) -> broadcast::Receiver<Vec<u8>> {
        self.broadcast_tx.subscribe()
    }

    /// Handle incoming message from a client
    /// Returns optional response message to send back to the client
    pub async fn handle_message(&self, data: &[u8]) -> Option<Vec<u8>> {
        let (response_tx, response_rx) = oneshot::channel();
        self.cmd_tx
            .send(DocCommand::HandleMessage {
                data: data.to_vec(),
                response_tx,
            })
            .await
            .ok()?;
        response_rx.await.ok()?
    }

    /// Generate SyncStep1 message to send to a new client
    pub async fn create_sync_step1(&self) -> Vec<u8> {
        let (response_tx, response_rx) = oneshot::channel();
        if self
            .cmd_tx
            .send(DocCommand::CreateSyncStep1 { response_tx })
            .await
            .is_err()
        {
            return vec![];
        }
        response_rx.await.unwrap_or_default()
    }

    /// Generate current awareness state to send to a new client
    pub async fn create_awareness_update(&self) -> Option<Vec<u8>> {
        let (response_tx, response_rx) = oneshot::channel();
        self.cmd_tx
            .send(DocCommand::CreateAwarenessUpdate { response_tx })
            .await
            .ok()?;
        response_rx.await.ok()?
    }
}

/// Document actor that handles all document operations on a single thread
async fn doc_actor(
    awareness: Awareness,
    mut cmd_rx: mpsc::Receiver<DocCommand>,
    broadcast_tx: broadcast::Sender<Vec<u8>>,
) {
    while let Some(cmd) = cmd_rx.recv().await {
        match cmd {
            DocCommand::HandleMessage { data, response_tx } => {
                let result = handle_message_impl(&awareness, &data, &broadcast_tx);
                let _ = response_tx.send(result);
            }
            DocCommand::CreateSyncStep1 { response_tx } => {
                let result = create_sync_step1_impl(&awareness);
                let _ = response_tx.send(result);
            }
            DocCommand::CreateAwarenessUpdate { response_tx } => {
                let result = create_awareness_update_impl(&awareness);
                let _ = response_tx.send(result);
            }
        }
    }
}

fn handle_message_impl(
    awareness: &Awareness,
    data: &[u8],
    broadcast_tx: &broadcast::Sender<Vec<u8>>,
) -> Option<Vec<u8>> {
    if data.is_empty() {
        return None;
    }

    let msg_type = data[0];
    let payload = &data[1..];

    match msg_type {
        MSG_SYNC => handle_sync_message_impl(awareness, payload, broadcast_tx),
        MSG_AWARENESS => {
            handle_awareness_message_impl(awareness, payload, broadcast_tx);
            None
        }
        _ => {
            tracing::warn!("Unknown message type: {}", msg_type);
            None
        }
    }
}

fn handle_sync_message_impl(
    awareness: &Awareness,
    payload: &[u8],
    broadcast_tx: &broadcast::Sender<Vec<u8>>,
) -> Option<Vec<u8>> {
    let msg = Message::decode_v1(payload).ok()?;

    match msg {
        Message::Sync(sync_msg) => match sync_msg {
            SyncMessage::SyncStep1(sv) => {
                let doc = awareness.doc();
                let txn = doc.transact();
                let update = txn.encode_state_as_update_v1(&sv);

                let response = Message::Sync(SyncMessage::SyncStep2(update));
                let mut encoded = vec![MSG_SYNC];
                encoded.extend(response.encode_v1());
                Some(encoded)
            }
            SyncMessage::SyncStep2(update) => {
                let doc = awareness.doc();
                let mut txn = doc.transact_mut();
                if let Err(e) = txn.apply_update(yrs::Update::decode_v1(&update).unwrap()) {
                    tracing::error!("Failed to apply SyncStep2 update: {}", e);
                }
                None
            }
            SyncMessage::Update(update) => {
                {
                    let doc = awareness.doc();
                    let mut txn = doc.transact_mut();
                    if let Err(e) = txn.apply_update(yrs::Update::decode_v1(&update).unwrap()) {
                        tracing::error!("Failed to apply update: {}", e);
                        return None;
                    }
                }

                // Broadcast the update to all other clients
                let broadcast_msg = Message::Sync(SyncMessage::Update(update));
                let mut encoded = vec![MSG_SYNC];
                encoded.extend(broadcast_msg.encode_v1());
                let _ = broadcast_tx.send(encoded);

                None
            }
        },
        Message::Auth(_) => {
            tracing::debug!("Auth message received, ignoring");
            None
        }
        Message::AwarenessQuery => {
            let awareness_update = awareness.update().ok()?;
            let mut encoded = vec![MSG_AWARENESS];
            encoded.extend(awareness_update.encode_v1());
            Some(encoded)
        }
        Message::Awareness(update) => {
            handle_awareness_update_impl(awareness, update, broadcast_tx);
            None
        }
        Message::Custom(_, _) => {
            tracing::debug!("Custom message received, ignoring");
            None
        }
    }
}

fn handle_awareness_message_impl(
    awareness: &Awareness,
    payload: &[u8],
    broadcast_tx: &broadcast::Sender<Vec<u8>>,
) {
    if let Ok(update) = AwarenessUpdate::decode_v1(payload) {
        handle_awareness_update_impl(awareness, update, broadcast_tx);
    } else {
        tracing::error!("Failed to decode awareness update");
    }
}

fn handle_awareness_update_impl(
    awareness: &Awareness,
    update: AwarenessUpdate,
    broadcast_tx: &broadcast::Sender<Vec<u8>>,
) {
    let encoded_update = update.encode_v1();

    if let Err(e) = awareness.apply_update(update) {
        tracing::error!("Failed to apply awareness update: {}", e);
        return;
    }

    // Broadcast awareness update to all clients
    let mut encoded = vec![MSG_AWARENESS];
    encoded.extend(encoded_update);
    let _ = broadcast_tx.send(encoded);
}

fn create_sync_step1_impl(awareness: &Awareness) -> Vec<u8> {
    let doc = awareness.doc();
    let txn = doc.transact();
    let sv = txn.state_vector();
    let msg = Message::Sync(SyncMessage::SyncStep1(sv));
    let mut encoded = vec![MSG_SYNC];
    encoded.extend(msg.encode_v1());
    encoded
}

fn create_awareness_update_impl(awareness: &Awareness) -> Option<Vec<u8>> {
    let update = awareness.update().ok()?;
    let mut encoded = vec![MSG_AWARENESS];
    encoded.extend(update.encode_v1());
    Some(encoded)
}

