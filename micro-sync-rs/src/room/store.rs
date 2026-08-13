use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, Ordering};

use bytes::Bytes;
use redis::aio::{ConnectionManager, MultiplexedConnection};
use redis::streams::{StreamId, StreamRangeReply, StreamReadReply};
use redis::{AsyncCommands, FromRedisValue, RedisError, RedisResult, Script, Value};

use crate::config::{RedisConfig, SyncConfig, WorkerConfig};
use crate::room::protocol::AwarenessClient;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum EventKind {
    Document,
    Awareness,
}

impl EventKind {
    fn code(self) -> &'static str {
        match self {
            Self::Document => "d",
            Self::Awareness => "a",
        }
    }

    fn from_code(code: &str) -> Option<Self> {
        match code {
            "d" => Some(Self::Document),
            "a" => Some(Self::Awareness),
            _ => None,
        }
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct StreamOffset {
    value: Arc<str>,
    timestamp: u64,
    sequence: u64,
}

impl StreamOffset {
    pub(super) fn new(value: String) -> Self {
        let (timestamp, sequence) = parse_stream_id(&value).unwrap_or_default();
        Self {
            value: value.into(),
            timestamp,
            sequence,
        }
    }

    pub(super) fn zero() -> Self {
        Self {
            value: Arc::from("0-0"),
            timestamp: 0,
            sequence: 0,
        }
    }

    pub fn as_str(&self) -> &str {
        &self.value
    }

    pub fn is_after(&self, other: &Self) -> bool {
        (self.timestamp, self.sequence) > (other.timestamp, other.sequence)
    }
}

#[derive(Debug)]
pub struct RedisEvent {
    pub id: StreamOffset,
    pub kind: EventKind,
    pub origin: String,
    pub payload: Bytes,
}

#[derive(Debug)]
pub struct RoomState {
    pub document: Bytes,
    pub awareness: Vec<Bytes>,
    pub highwater: StreamOffset,
}

pub struct RedisSessionStore {
    client: redis::Client,
    connection: ConnectionManager,
    prefix: Arc<str>,
    sync: SyncConfig,
    worker: WorkerConfig,
    healthy: AtomicBool,
}

#[derive(Debug)]
struct Snapshot {
    revision: u64,
    through: StreamOffset,
    update: Bytes,
}

#[derive(Debug)]
struct DocumentBatch {
    highwater: Option<StreamOffset>,
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
            healthy: AtomicBool::new(true),
        })
    }

    pub fn sync_config(&self) -> &SyncConfig {
        &self.sync
    }

    pub fn worker_config(&self) -> &WorkerConfig {
        &self.worker
    }

    pub fn is_healthy(&self) -> bool {
        self.healthy.load(Ordering::Relaxed)
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

    pub async fn latest_event_id(&self, room_id: &str) -> RedisResult<StreamOffset> {
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
                    .map_or_else(StreamOffset::zero, |entry| StreamOffset::new(entry.id)))
            }
            Err(error) if is_missing_key_error(&error) => Ok(StreamOffset::zero()),
            Err(error) => {
                self.healthy.store(false, Ordering::Relaxed);
                Err(error)
            }
        }
    }

    pub async fn load_room(&self, room_id: &str) -> RedisResult<RoomState> {
        let script = Script::new(
            r#"
local snapshot = redis.call('HMGET', KEYS[1], 'through', 'update')
local through = snapshot[1] or '0-0'
local update = snapshot[2] or ''
local events = redis.call('XRANGE', KEYS[2], '(' .. through, '+')
local awareness = redis.call('HVALS', KEYS[3])
return {through, update, events, awareness}
"#,
        );
        let mut connection = self.connection.clone();
        let result = script
            .key(self.snapshot_key(room_id))
            .key(self.room_stream(room_id))
            .key(self.presence_key(room_id))
            .invoke_async::<(String, Bytes, StreamRangeReply, Vec<Bytes>)>(&mut connection)
            .await;
        self.record_health(&result);
        let (through, snapshot, events, awareness) = result?;
        let through = StreamOffset::new(through);
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
        cursor: &StreamOffset,
    ) -> RedisResult<DocumentBatch> {
        let mut connection = self.connection.clone();
        let start = format!("({}", cursor.as_str());
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

    pub async fn read_rooms(
        &self,
        connection: &mut MultiplexedConnection,
        rooms: &[(&str, StreamOffset)],
    ) -> RedisResult<Vec<(usize, Vec<RedisEvent>)>> {
        if rooms.is_empty() {
            return Ok(Vec::new());
        }
        let keys: Vec<String> = rooms
            .iter()
            .map(|(room, _)| self.room_stream(room))
            .collect();
        let mut command = redis::cmd("XREAD");
        command
            .arg("COUNT")
            .arg(self.sync.relay_batch_size.max(1))
            .arg("BLOCK")
            .arg(self.sync.relay_block_millis)
            .arg("STREAMS")
            .arg(&keys);
        for (_, cursor) in rooms {
            command.arg(cursor.as_str());
        }
        let result = command.query_async::<StreamReadReply>(connection).await;
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
                Err(error)
            }
        }
    }

    pub async fn append_event(
        &self,
        room_id: &str,
        kind: EventKind,
        origin: &str,
        payload: &[u8],
    ) -> RedisResult<()> {
        let script = Script::new(
            r#"
redis.call('XADD', KEYS[1], '*', 'kind', ARGV[1], 'origin', ARGV[2], 'payload', ARGV[3])
redis.call('PEXPIRE', KEYS[1], ARGV[4])
redis.call('PEXPIRE', KEYS[2], ARGV[4])
redis.call('PEXPIRE', KEYS[3], ARGV[4])
redis.call('PEXPIRE', KEYS[4], ARGV[4])
if ARGV[1] == 'd' and redis.call('SET', KEYS[5], '1', 'NX', 'PX', ARGV[5]) then
  redis.call('XADD', KEYS[6], '*', 'room', ARGV[6])
end
return 1
"#,
        );
        let mut connection = self.connection.clone();
        let result = script
            .key(self.room_stream(room_id))
            .key(self.snapshot_key(room_id))
            .key(self.presence_key(room_id))
            .key(self.presence_owners_key(room_id))
            .key(self.pending_key(room_id))
            .key(self.worker_stream())
            .arg(kind.code())
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

    pub async fn register_connection(&self, room_id: &str, connection_id: &str) -> RedisResult<()> {
        let script = Script::new(
            r#"
local redis_time = redis.call('TIME')
local now_ms = tonumber(redis_time[1]) * 1000 + math.floor(tonumber(redis_time[2]) / 1000)
local expires_at = now_ms + tonumber(ARGV[1])
redis.call('ZADD', KEYS[1], expires_at, ARGV[2])
redis.call('HSET', KEYS[2], ARGV[2], ARGV[3])
redis.call('ZADD', KEYS[3], expires_at, ARGV[2])
redis.call('PEXPIRE', KEYS[3], ARGV[4])
redis.call('PEXPIRE', KEYS[4], ARGV[4])
redis.call('PEXPIRE', KEYS[5], ARGV[4])
redis.call('PEXPIRE', KEYS[6], ARGV[4])
redis.call('PEXPIRE', KEYS[7], ARGV[4])
redis.call('PEXPIRE', KEYS[8], ARGV[4])
redis.call('PEXPIRE', KEYS[9], ARGV[4])
redis.call('PEXPIRE', KEYS[10], ARGV[4])
return 1
"#,
        );
        let mut connection = self.connection.clone();
        let result = script
            .key(self.connection_expiries_key())
            .key(self.connection_rooms_key())
            .key(self.room_leases_key(room_id))
            .key(self.room_stream(room_id))
            .key(self.snapshot_key(room_id))
            .key(self.presence_key(room_id))
            .key(self.presence_owners_key(room_id))
            .key(self.presence_clocks_key(room_id))
            .key(self.connection_clients_key(connection_id))
            .key(self.connection_disconnects_key(connection_id))
            .arg(self.sync.lease_timeout_seconds.max(1) * 1000)
            .arg(connection_id)
            .arg(room_id)
            .arg(self.active_ttl_millis())
            .invoke_async::<i32>(&mut connection)
            .await
            .map(|_| ());
        self.record_health(&result);
        result
    }

    pub async fn refresh_connection(&self, room_id: &str, connection_id: &str) -> RedisResult<()> {
        self.register_connection(room_id, connection_id).await
    }

    pub async fn update_awareness(
        &self,
        room_id: &str,
        connection_id: &str,
        client: &AwarenessClient,
    ) -> RedisResult<bool> {
        let script = Script::new(
            r#"
local previous_clock = tonumber(redis.call('HGET', KEYS[3], ARGV[2]) or '-1')
local incoming_clock = tonumber(ARGV[3])
if incoming_clock < previous_clock or
   (incoming_clock == previous_clock and
    (ARGV[4] ~= '1' or redis.call('HEXISTS', KEYS[1], ARGV[2]) == 0)) then
  return 0
end
redis.call('HSET', KEYS[3], ARGV[2], incoming_clock)
if ARGV[4] == '1' then
  redis.call('HDEL', KEYS[1], ARGV[2])
  redis.call('HDEL', KEYS[2], ARGV[2])
  redis.call('HDEL', KEYS[4], ARGV[2])
  redis.call('HDEL', KEYS[5], ARGV[2])
else
  redis.call('HSET', KEYS[1], ARGV[2], ARGV[5])
  redis.call('HSET', KEYS[2], ARGV[2], ARGV[1])
  redis.call('HSET', KEYS[4], ARGV[2], ARGV[2])
  redis.call('HSET', KEYS[5], ARGV[2], ARGV[6])
end
redis.call('XADD', KEYS[6], '*', 'kind', 'a', 'origin', ARGV[1], 'payload', ARGV[5])
redis.call('PEXPIRE', KEYS[1], ARGV[7])
redis.call('PEXPIRE', KEYS[2], ARGV[7])
redis.call('PEXPIRE', KEYS[3], ARGV[7])
redis.call('PEXPIRE', KEYS[4], ARGV[7])
redis.call('PEXPIRE', KEYS[5], ARGV[7])
redis.call('PEXPIRE', KEYS[6], ARGV[7])
return 1
"#,
        );
        let mut connection = self.connection.clone();
        let result = script
            .key(self.presence_key(room_id))
            .key(self.presence_owners_key(room_id))
            .key(self.presence_clocks_key(room_id))
            .key(self.connection_clients_key(connection_id))
            .key(self.connection_disconnects_key(connection_id))
            .key(self.room_stream(room_id))
            .arg(connection_id)
            .arg(client.client_id)
            .arg(client.clock)
            .arg(if client.removed { 1 } else { 0 })
            .arg(&client.update)
            .arg(&client.disconnect_update)
            .arg(self.active_ttl_millis())
            .invoke_async::<i32>(&mut connection)
            .await
            .map(|updated| updated != 0);
        self.record_health(&result);
        result
    }

    pub async fn read_awareness(&self, room_id: &str) -> RedisResult<Vec<Bytes>> {
        let mut connection = self.connection.clone();
        let result = connection.hvals(self.presence_key(room_id)).await;
        self.record_health(&result);
        result
    }

    pub async fn disconnect(
        &self,
        room_id: &str,
        connection_id: &str,
        require_expired: bool,
    ) -> RedisResult<()> {
        let script = Script::new(
            r#"
local score = redis.call('ZSCORE', KEYS[1], ARGV[1])
local redis_time = redis.call('TIME')
local now_ms = tonumber(redis_time[1]) * 1000 + math.floor(tonumber(redis_time[2]) / 1000)
if ARGV[2] == '1' and score and tonumber(score) > now_ms then
  return 0
end
local owned = redis.call('HKEYS', KEYS[3])
for _, field in ipairs(owned) do
  local client = redis.call('HGET', KEYS[3], field)
  local payload = redis.call('HGET', KEYS[4], field)
  if client and payload and redis.call('HGET', KEYS[6], client) == ARGV[1] then
    redis.call('HDEL', KEYS[5], client)
    redis.call('HDEL', KEYS[6], client)
    local current_clock = tonumber(redis.call('HGET', KEYS[7], client) or '-1')
    redis.call('HSET', KEYS[7], client, math.min(current_clock + 1, 4294967295))
    redis.call('XADD', KEYS[8], '*', 'kind', 'a', 'origin', ARGV[1], 'payload', payload)
  end
end
redis.call('DEL', KEYS[3])
redis.call('DEL', KEYS[4])
redis.call('ZREM', KEYS[1], ARGV[1])
redis.call('HDEL', KEYS[2], ARGV[1])
redis.call('ZREM', KEYS[9], ARGV[1])
redis.call('ZREMRANGEBYSCORE', KEYS[9], '-inf', now_ms)
local remaining = redis.call('ZCARD', KEYS[9])
if remaining == 0 then
  for i = 5, 10 do redis.call('PEXPIRE', KEYS[i], ARGV[3]) end
else
  for i = 5, 10 do redis.call('PEXPIRE', KEYS[i], ARGV[4]) end
end
return 1
"#,
        );
        let mut connection = self.connection.clone();
        let result = script
            .key(self.connection_expiries_key())
            .key(self.connection_rooms_key())
            .key(self.connection_clients_key(connection_id))
            .key(self.connection_disconnects_key(connection_id))
            .key(self.presence_key(room_id))
            .key(self.presence_owners_key(room_id))
            .key(self.presence_clocks_key(room_id))
            .key(self.room_stream(room_id))
            .key(self.room_leases_key(room_id))
            .key(self.snapshot_key(room_id))
            .arg(connection_id)
            .arg(if require_expired { 1 } else { 0 })
            .arg(self.sync.session_retention_seconds.max(1) * 1000)
            .arg(self.active_ttl_millis())
            .invoke_async::<i32>(&mut connection)
            .await
            .map(|_| ());
        self.record_health(&result);
        result
    }

    pub async fn expired_connections(&self, limit: usize) -> RedisResult<Vec<(String, String)>> {
        let script = Script::new(
            r#"
local redis_time = redis.call('TIME')
local now_ms = tonumber(redis_time[1]) * 1000 + math.floor(tonumber(redis_time[2]) / 1000)
local ids = redis.call('ZRANGEBYSCORE', KEYS[1], '-inf', now_ms, 'LIMIT', 0, ARGV[1])
local result = {}
for _, id in ipairs(ids) do
  local room = redis.call('HGET', KEYS[2], id)
  if room then
    table.insert(result, id)
    table.insert(result, room)
  else
    redis.call('ZREM', KEYS[1], id)
  end
end
return result
"#,
        );
        let mut connection = self.connection.clone();
        let values = script
            .key(self.connection_expiries_key())
            .key(self.connection_rooms_key())
            .arg(limit)
            .invoke_async::<Vec<String>>(&mut connection)
            .await?;
        self.healthy.store(true, Ordering::Relaxed);
        let mut values = values.into_iter();
        Ok(std::iter::from_fn(|| Some((values.next()?, values.next()?))).collect())
    }

    pub async fn ensure_worker_group(&self) -> RedisResult<()> {
        let mut connection = self.connection.clone();
        let result = redis::cmd("XGROUP")
            .arg("CREATE")
            .arg(self.worker_stream())
            .arg(self.worker_group())
            .arg("0")
            .arg("MKSTREAM")
            .query_async::<()>(&mut connection)
            .await;
        match result {
            Ok(()) => Ok(()),
            Err(error) if error.to_string().contains("BUSYGROUP") => Ok(()),
            Err(error) => Err(error),
        }
    }

    pub async fn read_worker_tasks(
        &self,
        connection: &mut MultiplexedConnection,
        consumer: &str,
        count: usize,
    ) -> RedisResult<Vec<(String, String)>> {
        let reply = redis::cmd("XREADGROUP")
            .arg("GROUP")
            .arg(self.worker_group())
            .arg(consumer)
            .arg("COUNT")
            .arg(count)
            .arg("BLOCK")
            .arg(1000)
            .arg("STREAMS")
            .arg(self.worker_stream())
            .arg(">")
            .query_async::<StreamReadReply>(connection)
            .await?;
        self.healthy.store(true, Ordering::Relaxed);
        Ok(worker_tasks(reply))
    }

    pub async fn claim_worker_tasks(
        &self,
        consumer: &str,
        count: usize,
    ) -> RedisResult<Vec<(String, String)>> {
        let mut connection = self.connection.clone();
        let reply = redis::cmd("XAUTOCLAIM")
            .arg(self.worker_stream())
            .arg(self.worker_group())
            .arg(consumer)
            .arg(self.worker.task_timeout_seconds * 1000)
            .arg("0-0")
            .arg("COUNT")
            .arg(count)
            .query_async::<redis::streams::StreamAutoClaimReply>(&mut connection)
            .await?;
        self.healthy.store(true, Ordering::Relaxed);
        Ok(reply
            .claimed
            .into_iter()
            .filter_map(|mut entry| {
                take_field::<String>(&mut entry.map, "room").map(|room| (entry.id, room))
            })
            .collect())
    }

    pub async fn compact_room(&self, room_id: &str) -> RedisResult<()> {
        let snapshot = self.read_snapshot(room_id).await?;
        let batch = self
            .document_batch_after(room_id, &snapshot.through)
            .await?;
        let Some(highwater) = batch.highwater else {
            self.clear_pending(room_id).await?;
            if self
                .document_batch_after(room_id, &snapshot.through)
                .await?
                .updates
                .is_empty()
            {
                return Ok(());
            }
            self.enqueue_compaction(room_id).await?;
            return Ok(());
        };
        let mut updates = batch.updates;
        if !snapshot.update.is_empty() {
            updates.insert(0, snapshot.update);
        }
        let merged = match updates.len() {
            0 => Bytes::new(),
            1 => updates.pop().expect("length checked"),
            _ => Bytes::from(
                yrs::merge_updates_v1(updates.iter().map(Bytes::as_ref)).map_err(document_error)?,
            ),
        };

        let script = Script::new(
            r#"
local revision = tonumber(redis.call('HGET', KEYS[1], 'revision') or '0')
if revision ~= tonumber(ARGV[1]) then return 0 end
local redis_time = redis.call('TIME')
local now_ms = tonumber(redis_time[1]) * 1000 + math.floor(tonumber(redis_time[2]) / 1000)
redis.call('ZREMRANGEBYSCORE', KEYS[4], '-inf', now_ms)
local active_leases = redis.call('ZCARD', KEYS[4])
local stream_ttl = redis.call('PTTL', KEYS[3])
if active_leases == 0 and stream_ttl == -2 then
  redis.call('DEL', KEYS[1])
  redis.call('DEL', KEYS[2])
  return 1
end
redis.call('HSET', KEYS[1], 'revision', revision + 1, 'through', ARGV[2], 'update', ARGV[3])
redis.call('DEL', KEYS[2])
if active_leases == 0 then
  local inactive_ttl = stream_ttl
  if inactive_ttl < 0 or inactive_ttl > tonumber(ARGV[4]) then inactive_ttl = tonumber(ARGV[4]) end
  redis.call('PEXPIRE', KEYS[1], inactive_ttl)
else
  redis.call('PEXPIRE', KEYS[1], ARGV[5])
end
local safety_ms = now_ms - tonumber(ARGV[6])
local through_ms = tonumber(string.match(ARGV[2], '^(%d+)-')) or 0
local trim_ms = math.min(safety_ms, through_ms)
if trim_ms > 0 then redis.call('XTRIM', KEYS[3], 'MINID', '~', trim_ms .. '-0') end
return 1
"#,
        );
        let mut connection = self.connection.clone();
        let committed = script
            .key(self.snapshot_key(room_id))
            .key(self.pending_key(room_id))
            .key(self.room_stream(room_id))
            .key(self.room_leases_key(room_id))
            .arg(snapshot.revision)
            .arg(highwater.as_str())
            .arg(merged.as_ref())
            .arg(self.sync.session_retention_seconds.max(1) * 1000)
            .arg(self.active_ttl_millis())
            .arg(self.sync.stream_safety_seconds * 1000)
            .invoke_async::<i32>(&mut connection)
            .await?;
        if committed == 0 {
            self.enqueue_compaction(room_id).await?;
            return Ok(());
        }
        if self
            .document_batch_after(room_id, &highwater)
            .await?
            .updates
            .is_empty()
        {
            return Ok(());
        }
        self.enqueue_compaction(room_id).await?;
        Ok(())
    }

    pub async fn acknowledge_worker_task(&self, task_id: &str) -> RedisResult<()> {
        let mut connection = self.connection.clone();
        let script = Script::new(
            r#"
redis.call('XACK', KEYS[1], ARGV[1], ARGV[2])
redis.call('XDEL', KEYS[1], ARGV[2])
return 1
"#,
        );
        script
            .key(self.worker_stream())
            .arg(self.worker_group())
            .arg(task_id)
            .invoke_async::<i32>(&mut connection)
            .await
            .map(|_| ())
    }

    async fn enqueue_compaction(&self, room_id: &str) -> RedisResult<()> {
        let script = Script::new(
            r#"
if redis.call('SET', KEYS[1], '1', 'NX', 'PX', ARGV[1]) then
  redis.call('XADD', KEYS[2], '*', 'room', ARGV[2])
  return 1
end
return 0
"#,
        );
        let mut connection = self.connection.clone();
        script
            .key(self.pending_key(room_id))
            .key(self.worker_stream())
            .arg(self.pending_ttl_millis())
            .arg(room_id)
            .invoke_async::<i32>(&mut connection)
            .await
            .map(|_| ())
    }

    async fn clear_pending(&self, room_id: &str) -> RedisResult<()> {
        let mut connection = self.connection.clone();
        connection.del(self.pending_key(room_id)).await
    }

    async fn read_snapshot(&self, room_id: &str) -> RedisResult<Snapshot> {
        let mut connection = self.connection.clone();
        let (revision, through, update): (Option<u64>, Option<String>, Option<Bytes>) =
            redis::cmd("HMGET")
                .arg(self.snapshot_key(room_id))
                .arg(&["revision", "through", "update"])
                .query_async(&mut connection)
                .await?;
        self.healthy.store(true, Ordering::Relaxed);
        Ok(Snapshot {
            revision: revision.unwrap_or(0),
            through: through.map_or_else(StreamOffset::zero, StreamOffset::new),
            update: update.unwrap_or_default(),
        })
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

fn parse_event(mut entry: StreamId) -> Option<RedisEvent> {
    Some(RedisEvent {
        id: StreamOffset::new(entry.id),
        kind: EventKind::from_code(&take_field::<String>(&mut entry.map, "kind")?)?,
        origin: take_field(&mut entry.map, "origin")?,
        payload: take_field(&mut entry.map, "payload")?,
    })
}

fn document_batch(reply: StreamRangeReply) -> DocumentBatch {
    let mut highwater = None;
    let mut updates = Vec::new();
    for mut entry in reply.ids {
        highwater = Some(StreamOffset::new(entry.id));
        if take_field::<String>(&mut entry.map, "kind").as_deref() == Some("d")
            && let Some(payload) = take_field(&mut entry.map, "payload")
        {
            updates.push(payload);
        }
    }
    DocumentBatch { highwater, updates }
}

fn worker_tasks(reply: StreamReadReply) -> Vec<(String, String)> {
    reply
        .keys
        .into_iter()
        .flat_map(|stream| stream.ids)
        .filter_map(|mut entry| {
            take_field::<String>(&mut entry.map, "room").map(|room| (entry.id, room))
        })
        .collect()
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

#[cfg(test)]
mod tests {
    use crate::config::{RedisConfig, SyncConfig, WorkerConfig};
    use crate::room::protocol::AwarenessClient;
    use std::time::{SystemTime, UNIX_EPOCH};
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
        assert!(StreamOffset::new("10-0".into()).is_after(&StreamOffset::new("9-99".into())));
        assert!(StreamOffset::new("10-2".into()).is_after(&StreamOffset::new("10-1".into())));
        assert!(!StreamOffset::new("10-1".into()).is_after(&StreamOffset::new("10-1".into())));
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
