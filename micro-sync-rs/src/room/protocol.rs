use std::collections::HashMap;

use yrs::sync::{AwarenessUpdate, awareness::AwarenessUpdateEntry};
use yrs::updates::decoder::Decode;
use yrs::updates::encoder::Encode;
use yrs::{ClientID, Doc, ReadTxn, StateVector, Transact, Update, diff_updates_v1};

pub const MSG_SYNC: u64 = 0;
pub const MSG_AWARENESS: u64 = 1;
pub const MSG_AUTH: u64 = 2;
pub const MSG_QUERY_AWARENESS: u64 = 3;

const SYNC_STEP1: u64 = 0;
const SYNC_STEP2: u64 = 1;
const SYNC_UPDATE: u64 = 2;

#[derive(Debug, PartialEq)]
pub enum ClientMessage {
    SyncStep1(Vec<u8>),
    DocumentUpdate(Vec<u8>),
    Awareness(AwarenessMessage),
    QueryAwareness,
    Ignored,
}

#[derive(Debug, PartialEq)]
pub struct AwarenessMessage {
    pub clients: Vec<AwarenessClient>,
}

#[derive(Debug, PartialEq)]
pub struct AwarenessClient {
    pub client_id: u64,
    pub clock: u32,
    pub removed: bool,
    pub update: Vec<u8>,
    pub disconnect_update: Vec<u8>,
}

pub fn decode_client_message(data: &[u8]) -> Result<ClientMessage, &'static str> {
    let (message_type, consumed) = read_var_uint(data).ok_or("invalid message type")?;
    let payload = &data[consumed..];
    match message_type {
        MSG_SYNC => decode_sync_message(payload),
        MSG_AWARENESS => decode_awareness_message(payload).map(ClientMessage::Awareness),
        MSG_QUERY_AWARENESS => Ok(ClientMessage::QueryAwareness),
        MSG_AUTH => Ok(ClientMessage::Ignored),
        _ => Err("unsupported message type"),
    }
}

fn decode_sync_message(payload: &[u8]) -> Result<ClientMessage, &'static str> {
    let (sync_type, consumed) = read_var_uint(payload).ok_or("invalid sync type")?;
    let (content, _) = read_var_uint8_array(&payload[consumed..]).ok_or("invalid sync payload")?;
    match sync_type {
        SYNC_STEP1 => {
            StateVector::decode_v1(content).map_err(|_| "invalid state vector")?;
            Ok(ClientMessage::SyncStep1(content.to_vec()))
        }
        SYNC_STEP2 | SYNC_UPDATE => {
            Update::decode_v1(content).map_err(|_| "invalid document update")?;
            Ok(ClientMessage::DocumentUpdate(content.to_vec()))
        }
        _ => Err("unsupported sync type"),
    }
}

fn decode_awareness_message(payload: &[u8]) -> Result<AwarenessMessage, &'static str> {
    let (encoded, _) = read_var_uint8_array(payload).ok_or("invalid awareness payload")?;
    let update = AwarenessUpdate::decode_v1(encoded).map_err(|_| "invalid awareness update")?;
    let clients = update
        .clients
        .iter()
        .map(|(client_id, entry)| AwarenessClient {
            client_id: client_id.get(),
            clock: entry.clock,
            removed: entry.json.as_ref() == "null",
            update: awareness_update(client_id.get(), entry.clock, entry.json.as_ref()),
            disconnect_update: awareness_update(
                client_id.get(),
                entry.clock.saturating_add(1),
                "null",
            ),
        })
        .collect();
    Ok(AwarenessMessage { clients })
}

pub fn initial_messages(
    document: &[u8],
    awareness_updates: &[Vec<u8>],
) -> Result<Vec<Vec<u8>>, String> {
    let state_vector = if document.is_empty() {
        StateVector::default().encode_v1()
    } else {
        yrs::encode_state_vector_from_update_v1(document)
            .map_err(|error| format!("failed to build state vector: {error}"))?
    };
    let full_update = if document.is_empty() {
        empty_document_update()
    } else {
        document.to_vec()
    };
    let mut messages = vec![
        encode_sync(SYNC_STEP1, &state_vector),
        encode_sync(SYNC_STEP2, &full_update),
    ];
    messages.push(encode_awareness(&merge_awareness_updates(
        awareness_updates,
    )?));
    Ok(messages)
}

pub fn sync_step2(document: &[u8], remote_state_vector: &[u8]) -> Result<Vec<u8>, String> {
    let diff = if document.is_empty() {
        empty_document_update()
    } else {
        diff_updates_v1(document, remote_state_vector)
            .map_err(|error| format!("failed to create sync diff: {error}"))?
    };
    Ok(encode_sync(SYNC_STEP2, &diff))
}

pub fn encode_document_update(update: &[u8]) -> Vec<u8> {
    encode_sync(SYNC_UPDATE, update)
}

pub fn encode_awareness(update: &[u8]) -> Vec<u8> {
    let mut message = Vec::with_capacity(update.len() + 8);
    write_var_uint(&mut message, MSG_AWARENESS);
    write_var_uint8_array(&mut message, update);
    message
}

pub fn merge_awareness_updates(updates: &[Vec<u8>]) -> Result<Vec<u8>, String> {
    let mut clients = HashMap::new();
    for encoded in updates {
        let update = AwarenessUpdate::decode_v1(encoded)
            .map_err(|error| format!("failed to decode stored awareness: {error}"))?;
        for (client_id, entry) in update.clients {
            let replace = clients
                .get(&client_id)
                .is_none_or(|current: &AwarenessUpdateEntry| {
                    current.clock < entry.clock
                        || (current.clock == entry.clock && entry.json.as_ref() == "null")
                });
            if replace {
                clients.insert(client_id, entry);
            }
        }
    }
    Ok(AwarenessUpdate { clients }.encode_v1())
}

fn awareness_update(client_id: u64, clock: u32, json: &str) -> Vec<u8> {
    let clients = HashMap::from([(
        ClientID::new(client_id),
        AwarenessUpdateEntry {
            clock,
            json: json.into(),
        },
    )]);
    AwarenessUpdate { clients }.encode_v1()
}

fn empty_document_update() -> Vec<u8> {
    Doc::new()
        .transact()
        .encode_state_as_update_v1(&StateVector::default())
}

fn encode_sync(sync_type: u64, payload: &[u8]) -> Vec<u8> {
    let mut message = Vec::with_capacity(payload.len() + 8);
    write_var_uint(&mut message, MSG_SYNC);
    write_var_uint(&mut message, sync_type);
    write_var_uint8_array(&mut message, payload);
    message
}

fn read_var_uint(data: &[u8]) -> Option<(u64, usize)> {
    let mut result = 0u64;
    let mut shift = 0u32;
    for (index, byte) in data.iter().copied().enumerate() {
        result |= u64::from(byte & 0x7f) << shift;
        if byte & 0x80 == 0 {
            return Some((result, index + 1));
        }
        shift += 7;
        if shift >= 64 {
            return None;
        }
    }
    None
}

fn write_var_uint(buffer: &mut Vec<u8>, mut value: u64) {
    loop {
        let mut byte = (value & 0x7f) as u8;
        value >>= 7;
        if value != 0 {
            byte |= 0x80;
        }
        buffer.push(byte);
        if value == 0 {
            break;
        }
    }
}

fn read_var_uint8_array(data: &[u8]) -> Option<(&[u8], usize)> {
    let (length, consumed) = read_var_uint(data)?;
    let length = usize::try_from(length).ok()?;
    let end = consumed.checked_add(length)?;
    if end <= data.len() {
        Some((&data[consumed..end], end))
    } else {
        None
    }
}

fn write_var_uint8_array(buffer: &mut Vec<u8>, data: &[u8]) {
    write_var_uint(buffer, data.len() as u64);
    buffer.extend_from_slice(data);
}

#[cfg(test)]
mod tests {
    use yrs::{ReadTxn, Text, updates::encoder::Encode};

    use super::*;

    #[test]
    fn document_update_round_trips_through_protocol() {
        let doc = Doc::new();
        doc.get_or_insert_text("content")
            .push(&mut doc.transact_mut(), "hello");
        let update = doc
            .transact()
            .encode_state_as_update_v1(&StateVector::default());
        let wire = encode_document_update(&update);

        assert_eq!(
            decode_client_message(&wire),
            Ok(ClientMessage::DocumentUpdate(update))
        );
    }

    #[test]
    fn awareness_accepts_empty_and_multiple_clients() {
        let empty = AwarenessUpdate {
            clients: HashMap::new(),
        }
        .encode_v1();
        assert!(matches!(
            decode_client_message(&encode_awareness(&empty)),
            Ok(ClientMessage::Awareness(AwarenessMessage { clients, .. })) if clients.is_empty()
        ));

        let update = AwarenessUpdate {
            clients: HashMap::from([
                (
                    ClientID::new(1),
                    AwarenessUpdateEntry {
                        clock: 2,
                        json: "{\"name\":\"one\"}".into(),
                    },
                ),
                (
                    ClientID::new(2),
                    AwarenessUpdateEntry {
                        clock: 3,
                        json: "null".into(),
                    },
                ),
            ]),
        }
        .encode_v1();
        assert!(matches!(
            decode_client_message(&encode_awareness(&update)),
            Ok(ClientMessage::Awareness(AwarenessMessage { clients, .. })) if clients.len() == 2
        ));
    }

    #[test]
    fn initial_messages_include_merged_document() {
        let doc = Doc::new();
        doc.get_or_insert_text("content")
            .push(&mut doc.transact_mut(), "hello");
        let update = doc
            .transact()
            .encode_state_as_update_v1(&StateVector::default());

        let messages = initial_messages(&update, &[]).unwrap();
        assert_eq!(messages.len(), 3);
        assert!(matches!(
            decode_client_message(&messages[0]),
            Ok(ClientMessage::SyncStep1(_))
        ));
        assert!(matches!(
            decode_client_message(&messages[1]),
            Ok(ClientMessage::DocumentUpdate(_))
        ));
    }

    #[test]
    fn malformed_messages_are_rejected() {
        for message in [
            vec![],
            vec![0x80],
            vec![MSG_SYNC as u8],
            vec![MSG_AWARENESS as u8, 3, 1],
        ] {
            assert!(decode_client_message(&message).is_err());
        }
    }

    #[test]
    fn awareness_merge_keeps_the_newest_clock() {
        let older = awareness_update(7, 1, "{\"name\":\"old\"}");
        let newer = awareness_update(7, 2, "{\"name\":\"new\"}");
        let merged = merge_awareness_updates(&[newer.clone(), older]).unwrap();
        let update = AwarenessUpdate::decode_v1(&merged).unwrap();
        let entry = update.clients.get(&ClientID::new(7)).unwrap();
        assert_eq!(entry.clock, 2);
        assert_eq!(entry.json.as_ref(), "{\"name\":\"new\"}");
    }
}
