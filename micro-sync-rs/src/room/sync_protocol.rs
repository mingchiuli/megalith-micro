use tokio::sync::{broadcast, mpsc, oneshot};
use yrs::sync::{Awareness, AwarenessUpdate};
use yrs::updates::decoder::Decode;
use yrs::updates::encoder::Encode;
use yrs::{Doc, ReadTxn, StateVector, Transact, Update};

/// Message types following y-websocket protocol
pub const MSG_SYNC: u8 = 0;
pub const MSG_AWARENESS: u8 = 1;
pub const MSG_AUTH: u8 = 2;
pub const MSG_QUERY_AWARENESS: u8 = 3;

/// Sync message sub-types (varint encoded in protocol, but 0/1/2 are single byte)
const SYNC_STEP1: u8 = 0;
const SYNC_STEP2: u8 = 1;
const SYNC_UPDATE: u8 = 2;

/// A tagged broadcast message that carries a sender ID to prevent echo
#[derive(Clone, Debug)]
pub struct BroadcastPayload {
    pub sender_id: u64,
    pub data: Vec<u8>,
}

/// Commands sent to the document actor
enum DocCommand {
    HandleMessage {
        sender_id: u64,
        data: Vec<u8>,
        response_tx: oneshot::Sender<Vec<Vec<u8>>>,
    },
    GetInitialMessages {
        response_tx: oneshot::Sender<Vec<Vec<u8>>>,
    },
}

/// BroadcastGroup manages document synchronization and awareness for a room
/// Uses an actor pattern to handle non-Send Awareness type
pub struct BroadcastGroup {
    cmd_tx: mpsc::Sender<DocCommand>,
    broadcast_tx: broadcast::Sender<BroadcastPayload>,
    next_connection_id: std::sync::atomic::AtomicU64,
}

impl BroadcastGroup {
    pub fn new(capacity: usize) -> Self {
        let (cmd_tx, cmd_rx) = mpsc::channel(64);
        let (broadcast_tx, _) = broadcast::channel(capacity);

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
            next_connection_id: std::sync::atomic::AtomicU64::new(1),
        }
    }

    /// Allocate a unique connection ID for echo prevention
    pub fn next_connection_id(&self) -> u64 {
        self.next_connection_id
            .fetch_add(1, std::sync::atomic::Ordering::Relaxed)
    }

    pub fn subscribe(&self) -> broadcast::Receiver<BroadcastPayload> {
        self.broadcast_tx.subscribe()
    }

    /// Handle incoming message from a client.
    /// Returns response messages to send back to the sender only.
    pub async fn handle_message(&self, sender_id: u64, data: &[u8]) -> Vec<Vec<u8>> {
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

    /// Get all initial messages needed for a new connection (SyncStep1 + SyncStep2 + AwarenessUpdate)
    pub async fn get_initial_messages(&self) -> Vec<Vec<u8>> {
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

/// Document actor that handles all document operations on a single thread
async fn doc_actor(
    mut awareness: Awareness,
    mut cmd_rx: mpsc::Receiver<DocCommand>,
    broadcast_tx: broadcast::Sender<BroadcastPayload>,
) {
    while let Some(cmd) = cmd_rx.recv().await {
        match cmd {
            DocCommand::HandleMessage {
                sender_id,
                data,
                response_tx,
            } => {
                let result =
                    handle_message_impl(&mut awareness, sender_id, &data, &broadcast_tx);
                let _ = response_tx.send(result);
            }
            DocCommand::GetInitialMessages { response_tx } => {
                let result = get_initial_messages_impl(&awareness);
                let _ = response_tx.send(result);
            }
        }
    }
}

/// Read a varint from the buffer, returning (value, bytes_consumed)
fn read_var_uint(data: &[u8]) -> Option<(u64, usize)> {
    let mut result: u64 = 0;
    let mut shift = 0u32;
    for (i, &byte) in data.iter().enumerate() {
        result |= ((byte & 0x7F) as u64) << shift;
        if byte & 0x80 == 0 {
            return Some((result, i + 1));
        }
        shift += 7;
        if shift >= 64 {
            return None;
        }
    }
    None
}

/// Write a varint to a buffer
fn write_var_uint(buf: &mut Vec<u8>, mut value: u64) {
    loop {
        let mut byte = (value & 0x7F) as u8;
        value >>= 7;
        if value > 0 {
            byte |= 0x80;
        }
        buf.push(byte);
        if value == 0 {
            break;
        }
    }
}

/// Read a varint-length-prefixed byte array
fn read_var_uint8_array(data: &[u8]) -> Option<(&[u8], usize)> {
    let (len, consumed) = read_var_uint(data)?;
    let len = len as usize;
    let end = consumed + len;
    if end > data.len() {
        return None;
    }
    Some((&data[consumed..end], end))
}

/// Write a varint-length-prefixed byte array
fn write_var_uint8_array(buf: &mut Vec<u8>, data: &[u8]) {
    write_var_uint(buf, data.len() as u64);
    buf.extend_from_slice(data);
}

fn handle_message_impl(
    awareness: &mut Awareness,
    sender_id: u64,
    data: &[u8],
    broadcast_tx: &broadcast::Sender<BroadcastPayload>,
) -> Vec<Vec<u8>> {
    if data.is_empty() {
        return vec![];
    }

    let Some((msg_type, consumed)) = read_var_uint(data) else {
        tracing::warn!("Failed to read message type varint");
        return vec![];
    };
    let payload = &data[consumed..];

    match msg_type as u8 {
        MSG_SYNC => handle_sync_message_impl(awareness, sender_id, payload, broadcast_tx),
        MSG_AWARENESS => {
            handle_awareness_message_impl(awareness, sender_id, payload, broadcast_tx);
            vec![]
        }
        MSG_QUERY_AWARENESS => {
            // Respond with full awareness state
            if let Ok(update) = awareness.update() {
                let encoded = update.encode_v1();
                let mut msg = Vec::new();
                write_var_uint(&mut msg, MSG_AWARENESS as u64);
                write_var_uint8_array(&mut msg, &encoded);
                vec![msg]
            } else {
                vec![]
            }
        }
        MSG_AUTH => {
            tracing::debug!("Auth message received, ignoring");
            vec![]
        }
        _ => {
            tracing::warn!("Unknown message type: {}", msg_type);
            vec![]
        }
    }
}

fn handle_sync_message_impl(
    awareness: &mut Awareness,
    sender_id: u64,
    payload: &[u8],
    broadcast_tx: &broadcast::Sender<BroadcastPayload>,
) -> Vec<Vec<u8>> {
    // The sync payload starts with a varint sub-type
    let Some((sync_type, consumed)) = read_var_uint(payload) else {
        tracing::warn!("Failed to read sync message sub-type");
        return vec![];
    };
    let sync_payload = &payload[consumed..];

    match sync_type as u8 {
        SYNC_STEP1 => {
            // Client sent SyncStep1 (their state vector)
            // We need to read the state vector and respond with SyncStep2
            let Some((sv_bytes, _)) = read_var_uint8_array(sync_payload) else {
                tracing::warn!("Failed to read state vector from SyncStep1");
                return vec![];
            };

            let Ok(sv) = StateVector::decode_v1(sv_bytes) else {
                tracing::error!("Failed to decode state vector");
                return vec![];
            };

            let doc = awareness.doc();
            let txn = doc.transact();
            let update = txn.encode_state_as_update_v1(&sv);

            // Build SyncStep2 response: MSG_SYNC + SYNC_STEP2 + varint_bytes(update)
            let mut response = Vec::new();
            write_var_uint(&mut response, MSG_SYNC as u64);
            write_var_uint(&mut response, SYNC_STEP2 as u64);
            write_var_uint8_array(&mut response, &update);

            vec![response]
        }
        SYNC_STEP2 => {
            // Client sent SyncStep2 (update data)
            let Some((update_bytes, _)) = read_var_uint8_array(sync_payload) else {
                tracing::warn!("Failed to read update from SyncStep2");
                return vec![];
            };

            match Update::decode_v1(update_bytes) {
                Ok(update) => {
                    let doc = awareness.doc();
                    let mut txn = doc.transact_mut();
                    if let Err(e) = txn.apply_update(update) {
                        tracing::error!("Failed to apply SyncStep2 update: {}", e);
                    } else {
                        // After applying, broadcast the update to other clients
                        let mut broadcast_msg = Vec::new();
                        write_var_uint(&mut broadcast_msg, MSG_SYNC as u64);
                        write_var_uint(&mut broadcast_msg, SYNC_UPDATE as u64);
                        write_var_uint8_array(&mut broadcast_msg, update_bytes);
                        let _ = broadcast_tx.send(BroadcastPayload {
                            sender_id,
                            data: broadcast_msg,
                        });
                    }
                }
                Err(e) => {
                    tracing::error!("Failed to decode SyncStep2 update: {}", e);
                }
            }

            vec![]
        }
        SYNC_UPDATE => {
            // Client sent an incremental update
            let Some((update_bytes, _)) = read_var_uint8_array(sync_payload) else {
                tracing::warn!("Failed to read update data");
                return vec![];
            };

            match Update::decode_v1(update_bytes) {
                Ok(update) => {
                    let doc = awareness.doc();
                    let mut txn = doc.transact_mut();
                    if let Err(e) = txn.apply_update(update) {
                        tracing::error!("Failed to apply update: {}", e);
                        return vec![];
                    }
                }
                Err(e) => {
                    tracing::error!("Failed to decode update: {}", e);
                    return vec![];
                }
            }

            // Broadcast the update to all other clients (re-broadcast original format)
            let mut broadcast_msg = Vec::new();
            write_var_uint(&mut broadcast_msg, MSG_SYNC as u64);
            write_var_uint(&mut broadcast_msg, SYNC_UPDATE as u64);
            write_var_uint8_array(&mut broadcast_msg, update_bytes);
            let _ = broadcast_tx.send(BroadcastPayload {
                sender_id,
                data: broadcast_msg,
            });

            vec![]
        }
        _ => {
            tracing::warn!("Unknown sync sub-type: {}", sync_type);
            vec![]
        }
    }
}

fn handle_awareness_message_impl(
    awareness: &mut Awareness,
    sender_id: u64,
    payload: &[u8],
    broadcast_tx: &broadcast::Sender<BroadcastPayload>,
) {
    // payload is a varint-length-prefixed awareness update
    let Some((update_bytes, _)) = read_var_uint8_array(payload) else {
        tracing::error!("Failed to read awareness update bytes");
        return;
    };

    match AwarenessUpdate::decode_v1(update_bytes) {
        Ok(update) => {
            // Encode before applying (apply consumes the update)
            let encoded = update.encode_v1();

            if let Err(e) = awareness.apply_update(update) {
                tracing::error!("Failed to apply awareness update: {}", e);
                return;
            }

            // Broadcast awareness update to all other clients
            let mut broadcast_msg = Vec::new();
            write_var_uint(&mut broadcast_msg, MSG_AWARENESS as u64);
            write_var_uint8_array(&mut broadcast_msg, &encoded);
            let _ = broadcast_tx.send(BroadcastPayload {
                sender_id,
                data: broadcast_msg,
            });
        }
        Err(e) => {
            tracing::error!("Failed to decode awareness update: {}", e);
        }
    }
}

/// Get all initial messages for a new connection
/// Sends: SyncStep1 (server's state vector) + SyncStep2 (full server state) + AwarenessUpdate
fn get_initial_messages_impl(awareness: &Awareness) -> Vec<Vec<u8>> {
    let mut messages = Vec::with_capacity(3);
    let doc = awareness.doc();
    let txn = doc.transact();

    // 1. SyncStep1: send server's state vector so client can send us what we're missing
    let sv = txn.state_vector();
    let sv_encoded = sv.encode_v1();
    let mut sync_step1 = Vec::new();
    write_var_uint(&mut sync_step1, MSG_SYNC as u64);
    write_var_uint(&mut sync_step1, SYNC_STEP1 as u64);
    write_var_uint8_array(&mut sync_step1, &sv_encoded);
    messages.push(sync_step1);

    // 2. SyncStep2: send full server state so client gets everything we have
    let empty_sv = StateVector::default();
    let update = txn.encode_state_as_update_v1(&empty_sv);
    let mut sync_step2 = Vec::new();
    write_var_uint(&mut sync_step2, MSG_SYNC as u64);
    write_var_uint(&mut sync_step2, SYNC_STEP2 as u64);
    write_var_uint8_array(&mut sync_step2, &update);
    messages.push(sync_step2);

    drop(txn);

    // 3. AwarenessUpdate: send current awareness state
    if let Ok(update) = awareness.update() {
        let encoded = update.encode_v1();
        let mut awareness_msg = Vec::new();
        write_var_uint(&mut awareness_msg, MSG_AWARENESS as u64);
        write_var_uint8_array(&mut awareness_msg, &encoded);
        messages.push(awareness_msg);
    }

    messages
}