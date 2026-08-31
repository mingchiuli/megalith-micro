use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicU64, AtomicUsize, Ordering};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

use bytes::Bytes;
use tokio::sync::{Mutex, Notify, RwLock, broadcast};

use crate::application::error::StoreResult;
use crate::application::port::{
    CollaborationStore, CompactionStore, CompactionTaskReader, RoomEventReader, RoomEventStore,
};
use crate::domain::model::{EventCursor, EventKind, RoomCursor, RoomState};
use crate::domain::protocol::{AwarenessClient, encode_awareness, encode_document_update};

const STORE_RETRY_DELAY: Duration = Duration::from_secs(1);
const HEALTH_INTERVAL: Duration = Duration::from_secs(2);
const EXPIRED_CONNECTION_BATCH_SIZE: usize = 500;

#[derive(Clone, Debug)]
pub(crate) enum RelayMessage {
    Event(Arc<RelayEvent>),
    StoreUnavailable,
}

#[derive(Debug)]
pub(crate) struct RelayEvent {
    pub(crate) id: EventCursor,
    pub(crate) origin: String,
    pub(crate) frame: Bytes,
}

impl RelayEvent {
    pub(crate) fn should_forward_to(
        &self,
        connection_id: &str,
        initial_highwater: &EventCursor,
    ) -> bool {
        self.origin != connection_id && self.id.is_after(initial_highwater)
    }
}

struct RelayRoom {
    room_id: Arc<str>,
    sender: broadcast::Sender<RelayMessage>,
    cursor: Mutex<EventCursor>,
    subscribers: AtomicUsize,
}

pub(crate) struct RoomSubscription {
    room_id: Arc<str>,
    room: Arc<RelayRoom>,
    pub(crate) receiver: broadcast::Receiver<RelayMessage>,
}

impl RoomSubscription {
    pub(crate) fn room_id(&self) -> &str {
        &self.room_id
    }

    pub(crate) fn shared_room_id(&self) -> Arc<str> {
        Arc::clone(&self.room_id)
    }
}

#[derive(Clone, Debug)]
pub(crate) struct RoomManagerOptions {
    pub(crate) connection_buffer: usize,
    pub(crate) initial_sync_timeout: Duration,
    pub(crate) lease_heartbeat: Duration,
    pub(crate) worker_task_debounce: Duration,
    pub(crate) worker_task_timeout: Duration,
    pub(crate) worker_concurrency: usize,
}

pub(crate) struct RoomManager<S> {
    store: Arc<S>,
    options: RoomManagerOptions,
    rooms: RwLock<HashMap<Arc<str>, Arc<RelayRoom>>>,
    wake_relay: Notify,
    node_id: String,
    next_connection_id: AtomicU64,
}

impl<S> RoomManager<S>
where
    S: CollaborationStore,
{
    pub(crate) fn new(store: S, options: RoomManagerOptions) -> Arc<Self> {
        Arc::new(Self {
            store: Arc::new(store),
            options,
            rooms: RwLock::new(HashMap::new()),
            wake_relay: Notify::new(),
            node_id: format!("{}-{}", std::process::id(), now_nanos()),
            next_connection_id: AtomicU64::new(1),
        })
    }

    pub(crate) fn start(self: &Arc<Self>) {
        tokio::spawn(self.clone().relay_loop());
        tokio::spawn(self.clone().health_loop());
        tokio::spawn(self.clone().expired_connection_loop());
        tokio::spawn(self.clone().worker_supervisor());
    }

    pub(crate) fn next_connection_id(&self) -> String {
        format!(
            "{}-{}",
            self.node_id,
            self.next_connection_id.fetch_add(1, Ordering::Relaxed)
        )
    }

    pub(crate) fn initial_sync_timeout(&self) -> Duration {
        self.options.initial_sync_timeout
    }

    pub(crate) fn lease_heartbeat(&self) -> Duration {
        self.options.lease_heartbeat
    }

    pub(crate) async fn join(&self, room_id: Arc<str>) -> StoreResult<RoomSubscription> {
        if let Some(room) = self.rooms.read().await.get(room_id.as_ref()).cloned() {
            room.subscribers.fetch_add(1, Ordering::Relaxed);
            return Ok(RoomSubscription {
                room_id: Arc::clone(&room.room_id),
                receiver: room.sender.subscribe(),
                room,
            });
        }

        let cursor = self.store.latest_event_id(&room_id).await?;
        let mut rooms = self.rooms.write().await;
        let room = if let Some(room) = rooms.get(room_id.as_ref()).cloned() {
            room.subscribers.fetch_add(1, Ordering::Relaxed);
            room
        } else {
            let (sender, _) = broadcast::channel(self.options.connection_buffer.max(1));
            let room = Arc::new(RelayRoom {
                room_id: Arc::clone(&room_id),
                sender,
                cursor: Mutex::new(cursor),
                subscribers: AtomicUsize::new(1),
            });
            rooms.insert(Arc::clone(&room_id), Arc::clone(&room));
            self.wake_relay.notify_one();
            room
        };
        Ok(RoomSubscription {
            room_id: Arc::clone(&room.room_id),
            receiver: room.sender.subscribe(),
            room,
        })
    }

    pub(crate) async fn leave(&self, subscription: RoomSubscription) {
        let previous = subscription.room.subscribers.fetch_update(
            Ordering::Relaxed,
            Ordering::Relaxed,
            |count| count.checked_sub(1),
        );
        if previous != Ok(1) {
            return;
        }
        let mut rooms = self.rooms.write().await;
        if rooms.get(subscription.room_id()).is_some_and(|current| {
            Arc::ptr_eq(current, &subscription.room)
                && current.subscribers.load(Ordering::Relaxed) == 0
        }) {
            rooms.remove(subscription.room_id());
        }
    }

    pub(crate) fn is_ready(&self) -> bool {
        self.store.is_ready()
    }

    pub(crate) async fn register_connection(
        &self,
        room_id: &str,
        connection_id: &str,
    ) -> StoreResult<()> {
        self.store.register_connection(room_id, connection_id).await
    }

    pub(crate) async fn load_room(&self, room_id: &str) -> StoreResult<RoomState> {
        self.store.load_room(room_id).await
    }

    pub(crate) async fn append_document_update(
        &self,
        room_id: &str,
        connection_id: &str,
        update: &[u8],
    ) -> StoreResult<()> {
        self.store
            .append_event(room_id, EventKind::Document, connection_id, update)
            .await
    }

    pub(crate) async fn update_awareness(
        &self,
        room_id: &str,
        connection_id: &str,
        client: &AwarenessClient,
    ) -> StoreResult<bool> {
        self.store
            .update_awareness(room_id, connection_id, client)
            .await
    }

    pub(crate) async fn read_awareness(&self, room_id: &str) -> StoreResult<Vec<Bytes>> {
        self.store.read_awareness(room_id).await
    }

    pub(crate) async fn refresh_connection(
        &self,
        room_id: &str,
        connection_id: &str,
    ) -> StoreResult<()> {
        self.store.refresh_connection(room_id, connection_id).await
    }

    pub(crate) async fn disconnect(&self, room_id: &str, connection_id: &str) -> StoreResult<()> {
        self.store.disconnect(room_id, connection_id, false).await
    }

    async fn relay_loop(self: Arc<Self>) {
        let mut reader = self.connect_event_reader().await;
        loop {
            let rooms: Vec<(Arc<str>, Arc<RelayRoom>)> = self
                .rooms
                .read()
                .await
                .iter()
                .map(|(id, room)| (Arc::clone(id), Arc::clone(room)))
                .collect();
            if rooms.is_empty() {
                self.wake_relay.notified().await;
                continue;
            }
            let mut query = Vec::with_capacity(rooms.len());
            for (room_id, room) in &rooms {
                query.push(RoomCursor {
                    room_id: Arc::clone(room_id),
                    cursor: room.cursor.lock().await.clone(),
                });
            }

            match reader.read(&query).await {
                Ok(events_by_room) => {
                    for (room_index, events) in events_by_room {
                        let Some((_, room)) = rooms.get(room_index) else {
                            continue;
                        };
                        for event in events {
                            let frame = match event.kind {
                                EventKind::Document => encode_document_update(&event.payload),
                                EventKind::Awareness => encode_awareness(&event.payload),
                            };
                            let relay_event = Arc::new(RelayEvent {
                                id: event.id,
                                origin: event.origin,
                                frame,
                            });
                            *room.cursor.lock().await = relay_event.id.clone();
                            let _ = room.sender.send(RelayMessage::Event(relay_event));
                        }
                    }
                }
                Err(error) => {
                    tracing::error!(%error, "collaboration room relay failed");
                    self.broadcast_store_failure(&rooms);
                    tokio::time::sleep(STORE_RETRY_DELAY).await;
                    reader = self.connect_event_reader().await;
                }
            }
        }
    }

    async fn connect_event_reader(&self) -> <S as RoomEventStore>::Reader {
        loop {
            match self.store.event_reader().await {
                Ok(reader) => return reader,
                Err(error) => {
                    tracing::error!(%error, "failed to connect collaboration room relay");
                    tokio::time::sleep(STORE_RETRY_DELAY).await;
                }
            }
        }
    }

    fn broadcast_store_failure(&self, rooms: &[(Arc<str>, Arc<RelayRoom>)]) {
        for (_, room) in rooms {
            let _ = room.sender.send(RelayMessage::StoreUnavailable);
        }
    }

    async fn health_loop(self: Arc<Self>) {
        let mut interval = tokio::time::interval(HEALTH_INTERVAL);
        loop {
            interval.tick().await;
            if let Err(error) = self.store.ping().await {
                tracing::warn!(%error, "collaboration store health check failed");
            }
        }
    }

    async fn expired_connection_loop(self: Arc<Self>) {
        let mut interval =
            tokio::time::interval(self.options.lease_heartbeat.max(Duration::from_secs(1)));
        loop {
            interval.tick().await;
            let expired = match self
                .store
                .expired_connections(EXPIRED_CONNECTION_BATCH_SIZE)
                .await
            {
                Ok(expired) => expired,
                Err(error) => {
                    tracing::warn!(%error, "failed to query expired collaboration connections");
                    continue;
                }
            };
            for (connection_id, room_id) in expired {
                if let Err(error) = self.store.disconnect(&room_id, &connection_id, true).await {
                    tracing::warn!(%error, %room_id, %connection_id, "failed to expire collaboration connection");
                }
            }
        }
    }

    async fn worker_supervisor(self: Arc<Self>) {
        for index in 0..self.options.worker_concurrency.max(1) {
            tokio::spawn(
                self.clone()
                    .worker_loop(format!("{}-{index}", self.node_id)),
            );
        }
    }

    async fn worker_loop(self: Arc<Self>, consumer: String) {
        let claim_period = self.options.worker_task_timeout.max(Duration::from_secs(2)) / 2;
        let mut claim_interval = tokio::time::interval(claim_period);
        let mut reader = self.connect_task_reader().await;
        loop {
            let tasks = tokio::select! {
                _ = claim_interval.tick() => self.store.claim_tasks(&consumer, 1).await,
                tasks = reader.read(&consumer, 1) => tasks,
            };
            let tasks = match tasks {
                Ok(tasks) => tasks,
                Err(error) => {
                    tracing::warn!(%error, %consumer, "collaboration compaction worker read failed");
                    tokio::time::sleep(STORE_RETRY_DELAY).await;
                    reader = self.connect_task_reader().await;
                    continue;
                }
            };
            for task in tasks {
                tokio::time::sleep(self.options.worker_task_debounce).await;
                match self.store.compact_room(&task.room_id).await {
                    Ok(()) => {
                        if let Err(error) = self.store.acknowledge_task(&task.id).await {
                            tracing::warn!(%error, task_id = %task.id, "failed to acknowledge compaction task");
                        }
                    }
                    Err(error) => {
                        tracing::error!(%error, room_id = %task.room_id, task_id = %task.id, "room compaction failed");
                    }
                }
            }
        }
    }

    async fn connect_task_reader(&self) -> <S as CompactionStore>::Reader {
        loop {
            match self.store.task_reader().await {
                Ok(reader) => return reader,
                Err(error) => {
                    tracing::warn!(%error, "failed to connect collaboration compaction worker");
                    tokio::time::sleep(STORE_RETRY_DELAY).await;
                }
            }
        }
    }
}

fn now_nanos() -> u128 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_nanos()
}

#[cfg(test)]
mod tests {
    use std::sync::atomic::AtomicUsize;

    use super::*;
    use crate::application::error::{StoreError, StoreResult};
    use crate::application::port::{
        CompactionStore, CompactionTaskReader, PresenceStore, RoomEventReader, RoomEventStore,
        StoreHealth,
    };
    use crate::domain::model::{CompactionTask, RoomEvent};

    struct EmptyEventReader;

    impl RoomEventReader for EmptyEventReader {
        async fn read(
            &mut self,
            _rooms: &[RoomCursor],
        ) -> StoreResult<Vec<(usize, Vec<RoomEvent>)>> {
            Ok(Vec::new())
        }
    }

    struct EmptyTaskReader;

    impl CompactionTaskReader for EmptyTaskReader {
        async fn read(
            &mut self,
            _consumer: &str,
            _count: usize,
        ) -> StoreResult<Vec<CompactionTask>> {
            Ok(Vec::new())
        }
    }

    #[derive(Default)]
    struct FakeStore {
        latest_calls: AtomicUsize,
        refresh_calls: AtomicUsize,
    }

    impl RoomEventStore for FakeStore {
        type Reader = EmptyEventReader;

        async fn event_reader(&self) -> StoreResult<Self::Reader> {
            Ok(EmptyEventReader)
        }

        async fn latest_event_id(&self, _room_id: &str) -> StoreResult<EventCursor> {
            self.latest_calls.fetch_add(1, Ordering::Relaxed);
            Ok(EventCursor::default())
        }

        async fn load_room(&self, _room_id: &str) -> StoreResult<RoomState> {
            Err(StoreError::new("not used"))
        }

        async fn append_event(
            &self,
            _room_id: &str,
            _kind: EventKind,
            _origin: &str,
            _payload: &[u8],
        ) -> StoreResult<()> {
            Ok(())
        }
    }

    impl PresenceStore for FakeStore {
        async fn register_connection(
            &self,
            _room_id: &str,
            _connection_id: &str,
        ) -> StoreResult<()> {
            Ok(())
        }

        async fn refresh_connection(
            &self,
            _room_id: &str,
            _connection_id: &str,
        ) -> StoreResult<()> {
            self.refresh_calls.fetch_add(1, Ordering::Relaxed);
            Ok(())
        }

        async fn update_awareness(
            &self,
            _room_id: &str,
            _connection_id: &str,
            _client: &AwarenessClient,
        ) -> StoreResult<bool> {
            Ok(false)
        }

        async fn read_awareness(&self, _room_id: &str) -> StoreResult<Vec<Bytes>> {
            Ok(Vec::new())
        }

        async fn disconnect(
            &self,
            _room_id: &str,
            _connection_id: &str,
            _require_expired: bool,
        ) -> StoreResult<()> {
            Ok(())
        }

        async fn expired_connections(&self, _limit: usize) -> StoreResult<Vec<(String, String)>> {
            Ok(Vec::new())
        }
    }

    impl CompactionStore for FakeStore {
        type Reader = EmptyTaskReader;

        async fn task_reader(&self) -> StoreResult<Self::Reader> {
            Ok(EmptyTaskReader)
        }

        async fn claim_tasks(
            &self,
            _consumer: &str,
            _count: usize,
        ) -> StoreResult<Vec<CompactionTask>> {
            Ok(Vec::new())
        }

        async fn compact_room(&self, _room_id: &str) -> StoreResult<()> {
            Ok(())
        }

        async fn acknowledge_task(&self, _task_id: &str) -> StoreResult<()> {
            Ok(())
        }
    }

    impl StoreHealth for FakeStore {
        fn is_ready(&self) -> bool {
            true
        }

        async fn ping(&self) -> StoreResult<()> {
            Ok(())
        }
    }

    fn options() -> RoomManagerOptions {
        RoomManagerOptions {
            connection_buffer: 8,
            initial_sync_timeout: Duration::from_secs(1),
            lease_heartbeat: Duration::from_secs(1),
            worker_task_debounce: Duration::ZERO,
            worker_task_timeout: Duration::from_secs(2),
            worker_concurrency: 1,
        }
    }

    #[tokio::test]
    async fn repeated_room_joins_load_the_store_cursor_once() {
        let manager = RoomManager::new(FakeStore::default(), options());

        let first = manager.join(Arc::from("room")).await.unwrap();
        let second = manager.join(Arc::from("room")).await.unwrap();

        assert_eq!(manager.store.latest_calls.load(Ordering::Relaxed), 1);
        manager.leave(first).await;
        manager.leave(second).await;

        let third = manager.join(Arc::from("room")).await.unwrap();
        assert_eq!(manager.store.latest_calls.load(Ordering::Relaxed), 2);
        manager.leave(third).await;
    }

    #[tokio::test]
    async fn session_operations_use_the_store_port_and_propagate_errors() {
        let manager = RoomManager::new(FakeStore::default(), options());

        manager
            .refresh_connection("room", "connection")
            .await
            .unwrap();
        assert_eq!(manager.store.refresh_calls.load(Ordering::Relaxed), 1);
        assert_eq!(
            manager.load_room("room").await.unwrap_err().to_string(),
            "not used"
        );
    }

    #[test]
    fn relay_filters_the_origin_and_initial_cursor() {
        let initial = EventCursor::new(10, 1);
        let own_event = RelayEvent {
            id: EventCursor::new(10, 2),
            origin: "connection".to_string(),
            frame: Bytes::new(),
        };
        let old_event = RelayEvent {
            id: initial.clone(),
            origin: "other".to_string(),
            frame: Bytes::new(),
        };
        let new_event = RelayEvent {
            id: EventCursor::new(10, 2),
            origin: "other".to_string(),
            frame: Bytes::new(),
        };

        assert!(!own_event.should_forward_to("connection", &initial));
        assert!(!old_event.should_forward_to("connection", &initial));
        assert!(new_event.should_forward_to("connection", &initial));
    }

    #[test]
    fn relay_clones_share_the_encoded_frame() {
        let event = Arc::new(RelayEvent {
            id: EventCursor::default(),
            origin: "origin".to_string(),
            frame: Bytes::from(vec![1, 2, 3]),
        });
        let first = RelayMessage::Event(Arc::clone(&event));
        let second = first.clone();
        let (RelayMessage::Event(first), RelayMessage::Event(second)) = (first, second) else {
            panic!("expected relay events");
        };

        assert!(Arc::ptr_eq(&first, &second));
        assert_eq!(first.frame.as_ptr(), second.frame.as_ptr());
    }
}
