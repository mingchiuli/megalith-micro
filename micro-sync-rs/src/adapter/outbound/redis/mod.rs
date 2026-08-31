use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, Ordering};
use std::time::Duration;

use bytes::Bytes;
use redis::aio::{ConnectionManager, MultiplexedConnection};
use redis::streams::{StreamId, StreamRangeReply, StreamReadReply};
use redis::{AsyncCommands, FromRedisValue, RedisError, RedisResult, Script, Value};

use crate::application::error::{StoreError, StoreResult};
use crate::application::port::{
    CompactionStore, CompactionTaskReader, PresenceStore, RoomEventReader, RoomEventStore,
    StoreHealth,
};
use crate::config::{RedisConfig, SyncConfig, WorkerConfig};
use crate::domain::model::{
    CompactionTask, EventCursor, EventKind, RoomCursor, RoomEvent, RoomState,
};
use crate::domain::protocol::AwarenessClient;

mod compaction;
mod presence;
mod scripts;

const WORKER_READ_BLOCK_MILLIS: usize = 1_000;
const BLOCKING_RESPONSE_TIMEOUT_GRACE: Duration = Duration::from_secs(1);

pub(crate) struct RedisSessionStore {
    client: redis::Client,
    connection: ConnectionManager,
    prefix: Arc<str>,
    sync: SyncConfig,
    worker: WorkerConfig,
    healthy: Arc<AtomicBool>,
}

pub(crate) struct RedisEventReader {
    connection: MultiplexedConnection,
    prefix: Arc<str>,
    relay_block_millis: usize,
    relay_batch_size: usize,
    healthy: Arc<AtomicBool>,
}

#[derive(Debug)]
struct Snapshot {
    revision: u64,
    through: EventCursor,
    update: Bytes,
}

#[derive(Debug)]
struct DocumentBatch {
    highwater: Option<EventCursor>,
    updates: Vec<Bytes>,
}

impl RedisSessionStore {
    pub async fn connect(
        redis: RedisConfig,
        sync: SyncConfig,
        worker: WorkerConfig,
    ) -> RedisResult<Self> {
        let client = redis::Client::open(redis.url)?;
        let mut connection = client.get_connection_manager().await?;
        redis::cmd("PING")
            .query_async::<String>(&mut connection)
            .await?;
        Ok(Self {
            client,
            connection,
            prefix: redis.prefix.into(),
            sync,
            worker,
            healthy: Arc::new(AtomicBool::new(true)),
        })
    }

    pub async fn ping(&self) -> RedisResult<()> {
        let mut connection = self.connection.clone();
        let result = redis::cmd("PING")
            .query_async::<String>(&mut connection)
            .await
            .map(|_| ());
        self.record_health(&result);
        result
    }

    pub async fn dedicated_connection(&self) -> RedisResult<MultiplexedConnection> {
        self.client.get_multiplexed_async_connection().await
    }

    async fn latest_event_id(&self, room_id: &str) -> RedisResult<EventCursor> {
        let mut connection = self.connection.clone();
        let result = redis::cmd("XREVRANGE")
            .arg(self.room_stream(room_id))
            .arg("+")
            .arg("-")
            .arg("COUNT")
            .arg(1)
            .query_async::<StreamRangeReply>(&mut connection)
            .await;
        match result {
            Ok(reply) => {
                self.healthy.store(true, Ordering::Relaxed);
                Ok(reply
                    .ids
                    .into_iter()
                    .next()
                    .map_or_else(EventCursor::default, |entry| parse_event_cursor(&entry.id)))
            }
            Err(error) if is_missing_key_error(&error) => Ok(EventCursor::default()),
            Err(error) => {
                self.healthy.store(false, Ordering::Relaxed);
                Err(error)
            }
        }
    }

    async fn load_room(&self, room_id: &str) -> RedisResult<RoomState> {
        let script = Script::new(scripts::LOAD_ROOM);
        let mut connection = self.connection.clone();
        let result = script
            .key(self.snapshot_key(room_id))
            .key(self.room_stream(room_id))
            .key(self.presence_key(room_id))
            .invoke_async::<(String, Bytes, StreamRangeReply, Vec<Bytes>)>(&mut connection)
            .await;
        self.record_health(&result);
        let (through, snapshot, events, awareness) = result?;
        let through = parse_event_cursor(&through);
        let batch = document_batch(events);
        let highwater = batch.highwater.unwrap_or(through);
        let mut updates = batch.updates;
        if !snapshot.is_empty() {
            updates.insert(0, snapshot);
        }
        let document = match updates.len() {
            0 => Bytes::new(),
            1 => updates.pop().expect("length checked"),
            _ => Bytes::from(
                yrs::merge_updates_v1(updates.iter().map(Bytes::as_ref)).map_err(document_error)?,
            ),
        };
        Ok(RoomState {
            document,
            awareness,
            highwater,
        })
    }

    async fn document_batch_after(
        &self,
        room_id: &str,
        cursor: &EventCursor,
    ) -> RedisResult<DocumentBatch> {
        let mut connection = self.connection.clone();
        let start = format!("({}", format_event_cursor(cursor));
        let result = redis::cmd("XRANGE")
            .arg(self.room_stream(room_id))
            .arg(start)
            .arg("+")
            .query_async::<StreamRangeReply>(&mut connection)
            .await;
        match result {
            Ok(reply) => {
                self.healthy.store(true, Ordering::Relaxed);
                Ok(document_batch(reply))
            }
            Err(error) if is_missing_key_error(&error) => Ok(DocumentBatch {
                highwater: None,
                updates: Vec::new(),
            }),
            Err(error) => {
                self.healthy.store(false, Ordering::Relaxed);
                Err(error)
            }
        }
    }

    async fn append_event(
        &self,
        room_id: &str,
        kind: EventKind,
        origin: &str,
        payload: &[u8],
    ) -> RedisResult<()> {
        let script = Script::new(scripts::APPEND_EVENT);
        let mut connection = self.connection.clone();
        let result = script
            .key(self.room_stream(room_id))
            .key(self.snapshot_key(room_id))
            .key(self.presence_key(room_id))
            .key(self.presence_owners_key(room_id))
            .key(self.pending_key(room_id))
            .key(self.worker_stream())
            .arg(event_kind_code(kind))
            .arg(origin)
            .arg(payload)
            .arg(self.active_ttl_millis())
            .arg(self.pending_ttl_millis())
            .arg(room_id)
            .invoke_async::<i32>(&mut connection)
            .await
            .map(|_| ());
        self.record_health(&result);
        result
    }

    fn room_stream(&self, room_id: &str) -> String {
        self.room_key("events", room_id)
    }

    fn snapshot_key(&self, room_id: &str) -> String {
        self.room_key("snapshot", room_id)
    }

    fn presence_key(&self, room_id: &str) -> String {
        self.room_key("presence", room_id)
    }

    fn presence_owners_key(&self, room_id: &str) -> String {
        self.room_key("presence-owners", room_id)
    }

    fn presence_clocks_key(&self, room_id: &str) -> String {
        self.room_key("presence-clocks", room_id)
    }

    fn room_leases_key(&self, room_id: &str) -> String {
        self.room_key("leases", room_id)
    }

    fn pending_key(&self, room_id: &str) -> String {
        self.room_key("compact-pending", room_id)
    }

    fn room_key(&self, category: &str, room_id: &str) -> String {
        format!("{}:{category}:{}", self.prefix, encode_key(room_id))
    }

    fn worker_stream(&self) -> String {
        format!("{}:worker", self.prefix)
    }

    fn worker_group(&self) -> String {
        format!("{}:compactors", self.prefix)
    }

    fn connection_expiries_key(&self) -> String {
        format!("{}:connection-expiries", self.prefix)
    }

    fn connection_rooms_key(&self) -> String {
        format!("{}:connection-rooms", self.prefix)
    }

    fn connection_clients_key(&self, connection_id: &str) -> String {
        format!(
            "{}:connection-clients:{}",
            self.prefix,
            encode_key(connection_id)
        )
    }

    fn connection_disconnects_key(&self, connection_id: &str) -> String {
        format!(
            "{}:connection-disconnects:{}",
            self.prefix,
            encode_key(connection_id)
        )
    }

    fn active_ttl_millis(&self) -> u64 {
        (self.sync.session_retention_seconds.max(1) + self.sync.lease_timeout_seconds.max(1)) * 1000
    }

    fn pending_ttl_millis(&self) -> u64 {
        self.worker.task_timeout_seconds.max(30) * 2000
    }

    fn record_health<T>(&self, result: &RedisResult<T>) {
        self.healthy.store(result.is_ok(), Ordering::Relaxed);
    }
}

impl RoomEventStore for RedisSessionStore {
    type Reader = RedisEventReader;

    async fn event_reader(&self) -> StoreResult<Self::Reader> {
        let connection = self.dedicated_connection().await.map_err(store_error)?;
        Ok(RedisEventReader {
            connection,
            prefix: Arc::clone(&self.prefix),
            relay_block_millis: self.sync.relay_block_millis,
            relay_batch_size: self.sync.relay_batch_size,
            healthy: Arc::clone(&self.healthy),
        })
    }

    async fn latest_event_id(&self, room_id: &str) -> StoreResult<EventCursor> {
        RedisSessionStore::latest_event_id(self, room_id)
            .await
            .map_err(store_error)
    }

    async fn load_room(&self, room_id: &str) -> StoreResult<RoomState> {
        RedisSessionStore::load_room(self, room_id)
            .await
            .map_err(store_error)
    }

    async fn append_event(
        &self,
        room_id: &str,
        kind: EventKind,
        origin: &str,
        payload: &[u8],
    ) -> StoreResult<()> {
        RedisSessionStore::append_event(self, room_id, kind, origin, payload)
            .await
            .map_err(store_error)
    }
}

impl RoomEventReader for RedisEventReader {
    async fn read(&mut self, rooms: &[RoomCursor]) -> StoreResult<Vec<(usize, Vec<RoomEvent>)>> {
        if rooms.is_empty() {
            return Ok(Vec::new());
        }
        let keys: Vec<String> = rooms
            .iter()
            .map(|room| room_stream(&self.prefix, &room.room_id))
            .collect();
        let mut command = redis::cmd("XREAD");
        command
            .arg("COUNT")
            .arg(self.relay_batch_size.max(1))
            .arg("BLOCK")
            .arg(self.relay_block_millis)
            .arg("STREAMS")
            .arg(&keys);
        for room in rooms {
            command.arg(format_event_cursor(&room.cursor));
        }
        self.connection
            .set_response_timeout(blocking_response_timeout(self.relay_block_millis));
        let result = command
            .query_async::<StreamReadReply>(&mut self.connection)
            .await;
        match result {
            Ok(reply) => {
                self.healthy.store(true, Ordering::Relaxed);
                let room_by_key: HashMap<_, _> = keys
                    .iter()
                    .enumerate()
                    .map(|(index, key)| (key.as_str(), index))
                    .collect();
                Ok(reply
                    .keys
                    .into_iter()
                    .filter_map(|stream| {
                        let room = *room_by_key.get(stream.key.as_str())?;
                        let events = stream.ids.into_iter().filter_map(parse_event).collect();
                        Some((room, events))
                    })
                    .collect())
            }
            Err(error) => {
                self.healthy.store(false, Ordering::Relaxed);
                Err(store_error(error))
            }
        }
    }
}

impl StoreHealth for RedisSessionStore {
    fn is_ready(&self) -> bool {
        self.healthy.load(Ordering::Relaxed)
    }

    async fn ping(&self) -> StoreResult<()> {
        RedisSessionStore::ping(self).await.map_err(store_error)
    }
}

fn parse_event(mut entry: StreamId) -> Option<RoomEvent> {
    Some(RoomEvent {
        id: parse_event_cursor(&entry.id),
        kind: event_kind_from_code(&take_field::<String>(&mut entry.map, "kind")?)?,
        origin: take_field(&mut entry.map, "origin")?,
        payload: take_field(&mut entry.map, "payload")?,
    })
}

fn document_batch(reply: StreamRangeReply) -> DocumentBatch {
    let mut highwater = None;
    let mut updates = Vec::new();
    for mut entry in reply.ids {
        highwater = Some(parse_event_cursor(&entry.id));
        if take_field::<String>(&mut entry.map, "kind").as_deref() == Some("d")
            && let Some(payload) = take_field(&mut entry.map, "payload")
        {
            updates.push(payload);
        }
    }
    DocumentBatch { highwater, updates }
}

fn take_field<T: FromRedisValue>(map: &mut HashMap<String, Value>, key: &str) -> Option<T> {
    redis::from_redis_value(map.remove(key)?).ok()
}

fn document_error(error: yrs::encoding::read::Error) -> RedisError {
    RedisError::from((
        redis::ErrorKind::UnexpectedReturnType,
        "invalid Yjs update in Redis",
        error.to_string(),
    ))
}

fn is_missing_key_error(error: &RedisError) -> bool {
    error.kind() == redis::ErrorKind::Server(redis::ServerErrorKind::ResponseError)
        && error.to_string().contains("no such key")
}

fn encode_key(value: &str) -> String {
    const HEX: &[u8; 16] = b"0123456789abcdef";
    let mut encoded = String::with_capacity(value.len() * 2);
    for byte in value.as_bytes() {
        encoded.push(HEX[(byte >> 4) as usize] as char);
        encoded.push(HEX[(byte & 0x0f) as usize] as char);
    }
    encoded
}

fn parse_stream_id(value: &str) -> Option<(u64, u64)> {
    let (timestamp, sequence) = value.split_once('-')?;
    Some((timestamp.parse().ok()?, sequence.parse().ok()?))
}

fn parse_event_cursor(value: &str) -> EventCursor {
    let (timestamp, sequence) = parse_stream_id(value).unwrap_or_default();
    EventCursor::new(timestamp, sequence)
}

fn format_event_cursor(cursor: &EventCursor) -> String {
    let (timestamp, sequence) = cursor.parts();
    format!("{timestamp}-{sequence}")
}

fn event_kind_code(kind: EventKind) -> &'static str {
    match kind {
        EventKind::Document => "d",
        EventKind::Awareness => "a",
    }
}

fn event_kind_from_code(code: &str) -> Option<EventKind> {
    match code {
        "d" => Some(EventKind::Document),
        "a" => Some(EventKind::Awareness),
        _ => None,
    }
}

fn room_stream(prefix: &str, room_id: &str) -> String {
    format!("{prefix}:events:{}", encode_key(room_id))
}

fn store_error(error: RedisError) -> StoreError {
    StoreError::new(error.to_string())
}

fn blocking_response_timeout(block_millis: usize) -> Duration {
    let block_timeout = Duration::from_millis(u64::try_from(block_millis).unwrap_or(u64::MAX));
    block_timeout.saturating_add(BLOCKING_RESPONSE_TIMEOUT_GRACE)
}

#[cfg(test)]
mod tests {
    use crate::config::{RedisConfig, SyncConfig, WorkerConfig};
    use crate::domain::protocol::AwarenessClient;
    use std::time::{Instant, SystemTime, UNIX_EPOCH};
    use yrs::sync::{AwarenessUpdate, awareness::AwarenessUpdateEntry};
    use yrs::updates::encoder::Encode;
    use yrs::{ClientID, Doc, ReadTxn, StateVector, Text, Transact};

    use super::*;

    #[test]
    fn room_key_encoding_is_unambiguous() {
        assert_eq!(encode_key("a:b"), "613a62");
        assert_ne!(encode_key("ab"), encode_key("a:b"));
    }

    #[test]
    fn stream_ids_are_compared_numerically() {
        assert!(parse_event_cursor("10-0").is_after(&parse_event_cursor("9-99")));
        assert!(parse_event_cursor("10-2").is_after(&parse_event_cursor("10-1")));
        assert!(!parse_event_cursor("10-1").is_after(&parse_event_cursor("10-1")));
    }

    #[test]
    fn blocking_reads_outlast_the_redis_block_window() {
        assert_eq!(
            blocking_response_timeout(WORKER_READ_BLOCK_MILLIS),
            Duration::from_secs(2)
        );
        assert_eq!(blocking_response_timeout(200), Duration::from_millis(1_200));
    }

    #[test]
    fn stream_parser_moves_payload_into_shared_bytes() {
        let payload = vec![1, 2, 3, 4];
        let payload_ptr = payload.as_ptr();
        let entry = StreamId {
            id: "10-1".to_string(),
            map: HashMap::from([
                ("kind".to_string(), Value::BulkString(b"d".to_vec())),
                (
                    "origin".to_string(),
                    Value::BulkString(b"connection".to_vec()),
                ),
                ("payload".to_string(), Value::BulkString(payload)),
            ]),
            ..Default::default()
        };

        let event = parse_event(entry).unwrap();
        assert_eq!(event.payload.as_ptr(), payload_ptr);
        assert_eq!(event.payload.as_ref(), &[1, 2, 3, 4]);
    }

    #[tokio::test]
    #[ignore = "requires MICRO_SYNC_TEST_REDIS_URL"]
    async fn redis_store_round_trip_when_configured() {
        let url = std::env::var("MICRO_SYNC_TEST_REDIS_URL").unwrap();
        let prefix = format!("msync-test-{}", now_nanos_for_test());
        let sync = SyncConfig {
            session_retention_seconds: 5,
            lease_heartbeat_seconds: 1,
            lease_timeout_seconds: 3,
            stream_safety_seconds: 1,
            relay_block_millis: 10,
            relay_batch_size: 100,
            connection_buffer: 16,
            initial_sync_timeout_seconds: 2,
        };
        let worker = WorkerConfig {
            task_debounce_seconds: 0,
            task_timeout_seconds: 3,
            concurrency: 1,
        };
        let first = RedisSessionStore::connect(
            RedisConfig {
                url: url.clone(),
                prefix: prefix.clone(),
            },
            sync.clone(),
            worker.clone(),
        )
        .await
        .unwrap();
        let second = RedisSessionStore::connect(RedisConfig { url, prefix }, sync, worker)
            .await
            .unwrap();

        first.ensure_worker_group().await.unwrap();
        let mut worker_connection = first.dedicated_connection().await.unwrap();
        let read_started = Instant::now();
        assert!(
            first
                .read_worker_tasks(&mut worker_connection, "idle-worker", 1)
                .await
                .unwrap()
                .is_empty()
        );
        assert!(read_started.elapsed() >= Duration::from_millis(900));

        let doc = Doc::new();
        doc.get_or_insert_text("content")
            .push(&mut doc.transact_mut(), "shared");
        let update = doc
            .transact()
            .encode_state_as_update_v1(&StateVector::default());
        first
            .append_event("room", EventKind::Document, "first", &update)
            .await
            .unwrap();

        let mut connection = first.connection.clone();
        assert!(
            redis::cmd("EXISTS")
                .arg(first.pending_key("room"))
                .query_async::<bool>(&mut connection)
                .await
                .unwrap()
        );
        let tasks = redis::cmd("XLEN")
            .arg(first.worker_stream())
            .query_async::<usize>(&mut connection)
            .await
            .unwrap();
        assert_eq!(tasks, 1);

        let before_compaction = second.load_room("room").await.unwrap();
        assert_eq!(before_compaction.document, update);
        first.compact_room("room").await.unwrap();
        let after_compaction = second.load_room("room").await.unwrap();
        assert_eq!(after_compaction.document, update);

        let state_vector = doc.transact().state_vector();
        doc.get_or_insert_text("content")
            .push(&mut doc.transact_mut(), " increment");
        let incremental = doc.transact().encode_state_as_update_v1(&state_vector);
        first
            .append_event("room", EventKind::Document, "first", &incremental)
            .await
            .unwrap();
        let expected = yrs::merge_updates_v1([update.as_slice(), incremental.as_slice()]).unwrap();
        let snapshot_plus_incremental = second.load_room("room").await.unwrap();
        assert_eq!(snapshot_plus_incremental.document, expected);

        first
            .register_connection("room", "presence-connection")
            .await
            .unwrap();
        let online = awareness_client(42, 2, "{\"name\":\"current\"}");
        assert!(
            first
                .update_awareness("room", "presence-connection", &online)
                .await
                .unwrap()
        );
        let stale = awareness_client(42, 1, "{\"name\":\"stale\"}");
        assert!(
            !second
                .update_awareness("room", "stale-connection", &stale)
                .await
                .unwrap()
        );
        assert_eq!(
            second.read_awareness("room").await.unwrap(),
            vec![online.update]
        );
        assert_eq!(second.load_room("room").await.unwrap().awareness.len(), 1);

        first
            .disconnect("room", "presence-connection", false)
            .await
            .unwrap();
        assert!(second.read_awareness("room").await.unwrap().is_empty());

        let keys = redis::cmd("KEYS")
            .arg(format!("{}:*", first.prefix))
            .query_async::<Vec<String>>(&mut connection)
            .await
            .unwrap();
        if !keys.is_empty() {
            redis::cmd("DEL")
                .arg(keys)
                .query_async::<usize>(&mut connection)
                .await
                .unwrap();
        }
    }

    fn awareness_client(client_id: u64, clock: u32, json: &str) -> AwarenessClient {
        let encode = |clock, json: &str| {
            AwarenessUpdate {
                clients: HashMap::from([(
                    ClientID::new(client_id),
                    AwarenessUpdateEntry {
                        clock,
                        json: json.into(),
                    },
                )]),
            }
            .encode_v1()
        };
        AwarenessClient {
            client_id,
            clock,
            removed: json == "null",
            update: encode(clock, json),
            disconnect_update: encode(clock.saturating_add(1), "null"),
        }
    }

    fn now_nanos_for_test() -> u128 {
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap_or_default()
            .as_nanos()
    }
}
