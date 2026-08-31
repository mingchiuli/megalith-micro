use std::future::Future;

use bytes::Bytes;

use crate::application::error::StoreResult;
use crate::domain::model::{
    CompactionTask, EventCursor, EventKind, RoomCursor, RoomEvent, RoomState,
};
use crate::domain::protocol::AwarenessClient;

pub(crate) trait RoomEventReader: Send {
    fn read(
        &mut self,
        rooms: &[RoomCursor],
    ) -> impl Future<Output = StoreResult<Vec<(usize, Vec<RoomEvent>)>>> + Send;
}

pub(crate) trait RoomEventStore: Send + Sync + 'static {
    type Reader: RoomEventReader;

    fn event_reader(&self) -> impl Future<Output = StoreResult<Self::Reader>> + Send;

    fn latest_event_id(
        &self,
        room_id: &str,
    ) -> impl Future<Output = StoreResult<EventCursor>> + Send;

    fn load_room(&self, room_id: &str) -> impl Future<Output = StoreResult<RoomState>> + Send;

    fn append_event(
        &self,
        room_id: &str,
        kind: EventKind,
        origin: &str,
        payload: &[u8],
    ) -> impl Future<Output = StoreResult<()>> + Send;
}

pub(crate) trait PresenceStore: Send + Sync + 'static {
    fn register_connection(
        &self,
        room_id: &str,
        connection_id: &str,
    ) -> impl Future<Output = StoreResult<()>> + Send;

    fn refresh_connection(
        &self,
        room_id: &str,
        connection_id: &str,
    ) -> impl Future<Output = StoreResult<()>> + Send;

    fn update_awareness(
        &self,
        room_id: &str,
        connection_id: &str,
        client: &AwarenessClient,
    ) -> impl Future<Output = StoreResult<bool>> + Send;

    fn read_awareness(&self, room_id: &str)
    -> impl Future<Output = StoreResult<Vec<Bytes>>> + Send;

    fn disconnect(
        &self,
        room_id: &str,
        connection_id: &str,
        require_expired: bool,
    ) -> impl Future<Output = StoreResult<()>> + Send;

    fn expired_connections(
        &self,
        limit: usize,
    ) -> impl Future<Output = StoreResult<Vec<(String, String)>>> + Send;
}

pub(crate) trait CompactionTaskReader: Send {
    fn read(
        &mut self,
        consumer: &str,
        count: usize,
    ) -> impl Future<Output = StoreResult<Vec<CompactionTask>>> + Send;
}

pub(crate) trait CompactionStore: Send + Sync + 'static {
    type Reader: CompactionTaskReader;

    fn task_reader(&self) -> impl Future<Output = StoreResult<Self::Reader>> + Send;

    fn claim_tasks(
        &self,
        consumer: &str,
        count: usize,
    ) -> impl Future<Output = StoreResult<Vec<CompactionTask>>> + Send;

    fn compact_room(&self, room_id: &str) -> impl Future<Output = StoreResult<()>> + Send;

    fn acknowledge_task(&self, task_id: &str) -> impl Future<Output = StoreResult<()>> + Send;
}

pub(crate) trait StoreHealth: Send + Sync + 'static {
    fn is_ready(&self) -> bool;

    fn ping(&self) -> impl Future<Output = StoreResult<()>> + Send;
}

pub(crate) trait CollaborationStore:
    RoomEventStore + PresenceStore + CompactionStore + StoreHealth
{
}

impl<T> CollaborationStore for T where
    T: RoomEventStore + PresenceStore + CompactionStore + StoreHealth
{
}
