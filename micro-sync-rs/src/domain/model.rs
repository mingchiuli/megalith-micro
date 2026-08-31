use std::sync::Arc;

use bytes::Bytes;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum EventKind {
    Document,
    Awareness,
}

#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub(crate) struct EventCursor {
    timestamp: u64,
    sequence: u64,
}

impl EventCursor {
    pub(crate) const fn new(timestamp: u64, sequence: u64) -> Self {
        Self {
            timestamp,
            sequence,
        }
    }

    pub(crate) const fn parts(&self) -> (u64, u64) {
        (self.timestamp, self.sequence)
    }

    pub(crate) fn is_after(&self, other: &Self) -> bool {
        self.parts() > other.parts()
    }
}

#[derive(Debug)]
pub(crate) struct RoomEvent {
    pub(crate) id: EventCursor,
    pub(crate) kind: EventKind,
    pub(crate) origin: String,
    pub(crate) payload: Bytes,
}

#[derive(Debug)]
pub(crate) struct RoomState {
    pub(crate) document: Bytes,
    pub(crate) awareness: Vec<Bytes>,
    pub(crate) highwater: EventCursor,
}

#[derive(Clone, Debug)]
pub(crate) struct RoomCursor {
    pub(crate) room_id: Arc<str>,
    pub(crate) cursor: EventCursor,
}

#[derive(Debug)]
pub(crate) struct CompactionTask {
    pub(crate) id: String,
    pub(crate) room_id: String,
}
