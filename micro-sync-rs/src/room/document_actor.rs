use tokio::sync::{broadcast, mpsc, oneshot};
use yrs::sync::{Awareness, AwarenessUpdate};
use yrs::updates::decoder::Decode;
use yrs::updates::encoder::Encode;
use yrs::{ReadTxn, StateVector, Transact, Update};

#[cfg(test)]
use yrs::Doc;

/// Message types following y-websocket protocol
const MSG_SYNC: u8 = 0;
const MSG_AWARENESS: u8 = 1;
const MSG_AUTH: u8 = 2;
const MSG_QUERY_AWARENESS: u8 = 3;

/// Sync message sub-types
const SYNC_STEP1: u8 = 0;
const SYNC_STEP2: u8 = 1;
const SYNC_UPDATE: u8 = 2;

/// A tagged broadcast message that carries a sender ID to prevent echo
#[derive(Clone, Debug)]
pub(super) struct BroadcastPayload {
    pub sender_id: u64,
    pub data: Vec<u8>,
}

/// Commands sent to the document actor
pub(super) enum DocCommand {
    HandleMessage {
        sender_id: u64,
        data: Vec<u8>,
        response_tx: oneshot::Sender<Vec<Vec<u8>>>,
    },
    GetInitialMessages {
        response_tx: oneshot::Sender<Vec<Vec<u8>>>,
    },
}

pub(super) async fn run(
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
                let result = handle_message_impl(&mut awareness, sender_id, &data, &broadcast_tx);
                let _ = response_tx.send(result);
            }
            DocCommand::GetInitialMessages { response_tx } => {
                let result = get_initial_messages_impl(&awareness);
                let _ = response_tx.send(result);
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// Varint helpers
// ════════════════════════════════════════════════════════════════════════════

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

fn read_var_uint8_array(data: &[u8]) -> Option<(&[u8], usize)> {
    let (len, consumed) = read_var_uint(data)?;
    let len = len as usize;
    let end = consumed + len;
    if end > data.len() {
        return None;
    }
    Some((&data[consumed..end], end))
}

fn write_var_uint8_array(buf: &mut Vec<u8>, data: &[u8]) {
    write_var_uint(buf, data.len() as u64);
    buf.extend_from_slice(data);
}

// ════════════════════════════════════════════════════════════════════════════
// Protocol implementation (runs inside the actor thread)
// ════════════════════════════════════════════════════════════════════════════

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
    let Some((sync_type, consumed)) = read_var_uint(payload) else {
        tracing::warn!("Failed to read sync message sub-type");
        return vec![];
    };
    let sync_payload = &payload[consumed..];

    match sync_type as u8 {
        SYNC_STEP1 => {
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

            let mut response = Vec::new();
            write_var_uint(&mut response, MSG_SYNC as u64);
            write_var_uint(&mut response, SYNC_STEP2 as u64);
            write_var_uint8_array(&mut response, &update);

            vec![response]
        }
        SYNC_STEP2 => {
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
    let Some((update_bytes, _)) = read_var_uint8_array(payload) else {
        tracing::error!("Failed to read awareness update bytes");
        return;
    };

    match AwarenessUpdate::decode_v1(update_bytes) {
        Ok(update) => {
            let encoded = update.encode_v1();

            if let Err(e) = awareness.apply_update(update) {
                tracing::error!("Failed to apply awareness update: {}", e);
                return;
            }

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

fn get_initial_messages_impl(awareness: &Awareness) -> Vec<Vec<u8>> {
    let mut messages = Vec::with_capacity(3);
    let doc = awareness.doc();
    let txn = doc.transact();

    // 1. SyncStep1: server state vector
    let sv = txn.state_vector();
    let sv_encoded = sv.encode_v1();
    let mut sync_step1 = Vec::new();
    write_var_uint(&mut sync_step1, MSG_SYNC as u64);
    write_var_uint(&mut sync_step1, SYNC_STEP1 as u64);
    write_var_uint8_array(&mut sync_step1, &sv_encoded);
    messages.push(sync_step1);

    // 2. SyncStep2: full server state
    let empty_sv = StateVector::default();
    let update = txn.encode_state_as_update_v1(&empty_sv);
    let mut sync_step2 = Vec::new();
    write_var_uint(&mut sync_step2, MSG_SYNC as u64);
    write_var_uint(&mut sync_step2, SYNC_STEP2 as u64);
    write_var_uint8_array(&mut sync_step2, &update);
    messages.push(sync_step2);

    drop(txn);

    // 3. AwarenessUpdate
    if let Ok(update) = awareness.update() {
        let encoded = update.encode_v1();
        let mut awareness_msg = Vec::new();
        write_var_uint(&mut awareness_msg, MSG_AWARENESS as u64);
        write_var_uint8_array(&mut awareness_msg, &encoded);
        messages.push(awareness_msg);
    }

    messages
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn var_uint_round_trips_boundary_values() {
        for value in [0, 1, 127, 128, 16_384, u32::MAX as u64, u64::MAX] {
            let mut encoded = Vec::new();
            write_var_uint(&mut encoded, value);
            assert_eq!(read_var_uint(&encoded), Some((value, encoded.len())));
        }
    }

    #[test]
    fn malformed_var_uint_and_byte_array_are_rejected() {
        assert_eq!(read_var_uint(&[0x80; 10]), None);
        assert_eq!(read_var_uint8_array(&[3, 1, 2]), None);
    }

    #[test]
    fn initial_messages_contain_sync_steps_and_awareness() {
        let awareness = Awareness::new(Doc::new());
        let messages = get_initial_messages_impl(&awareness);

        assert_eq!(messages.len(), 3);
        assert_eq!(
            read_var_uint(&messages[0]).map(|value| value.0),
            Some(MSG_SYNC as u64)
        );
        let first_payload = &messages[0][read_var_uint(&messages[0]).unwrap().1..];
        assert_eq!(
            read_var_uint(first_payload).map(|value| value.0),
            Some(SYNC_STEP1 as u64)
        );
        assert_eq!(
            read_var_uint(&messages[1]).map(|value| value.0),
            Some(MSG_SYNC as u64)
        );
        let second_payload = &messages[1][read_var_uint(&messages[1]).unwrap().1..];
        assert_eq!(
            read_var_uint(second_payload).map(|value| value.0),
            Some(SYNC_STEP2 as u64)
        );
        assert_eq!(
            read_var_uint(&messages[2]).map(|value| value.0),
            Some(MSG_AWARENESS as u64)
        );
    }

    #[test]
    fn query_awareness_returns_an_awareness_message() {
        let mut awareness = Awareness::new(Doc::new());
        let (broadcast_tx, _) = broadcast::channel(4);
        let responses =
            handle_message_impl(&mut awareness, 7, &[MSG_QUERY_AWARENESS], &broadcast_tx);

        assert_eq!(responses.len(), 1);
        assert_eq!(
            read_var_uint(&responses[0]).map(|value| value.0),
            Some(MSG_AWARENESS as u64)
        );
    }

    #[test]
    fn valid_sync_update_is_broadcast_with_sender_id() {
        let doc = Doc::new();
        let update = doc
            .transact()
            .encode_state_as_update_v1(&StateVector::default());
        let mut message = vec![MSG_SYNC, SYNC_UPDATE];
        write_var_uint8_array(&mut message, &update);

        let mut awareness = Awareness::new(Doc::new());
        let (broadcast_tx, mut broadcast_rx) = broadcast::channel(4);
        let responses = handle_message_impl(&mut awareness, 42, &message, &broadcast_tx);

        assert!(responses.is_empty());
        let broadcast = broadcast_rx
            .try_recv()
            .expect("sync update should be broadcast");
        assert_eq!(broadcast.sender_id, 42);
        assert_eq!(broadcast.data, message);
    }

    #[test]
    fn malformed_protocol_messages_do_not_panic_or_broadcast() {
        let mut awareness = Awareness::new(Doc::new());
        let (broadcast_tx, mut broadcast_rx) = broadcast::channel(4);

        for message in [
            vec![],
            vec![0x80],
            vec![MSG_SYNC],
            vec![MSG_AWARENESS, 3, 1],
        ] {
            assert!(handle_message_impl(&mut awareness, 1, &message, &broadcast_tx).is_empty());
        }
        assert!(broadcast_rx.try_recv().is_err());
    }
}
