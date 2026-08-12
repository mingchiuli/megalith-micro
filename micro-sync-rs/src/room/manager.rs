use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicU64, AtomicUsize, Ordering};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

use tokio::sync::{Mutex, Notify, RwLock, broadcast};

use super::store::{RedisEvent, RedisSessionStore};

#[derive(Clone, Debug)]
pub enum RelayMessage {
    Event(RedisEvent),
    RedisUnavailable,
}

struct RelayRoom {
    sender: broadcast::Sender<RelayMessage>,
    cursor: Mutex<String>,
    subscribers: AtomicUsize,
}

pub struct RoomSubscription {
    room_id: String,
    room: Arc<RelayRoom>,
    pub receiver: broadcast::Receiver<RelayMessage>,
}

impl RoomSubscription {
    pub fn room_id(&self) -> &str {
        &self.room_id
    }
}

pub struct RoomManager {
    store: RedisSessionStore,
    rooms: RwLock<HashMap<String, Arc<RelayRoom>>>,
    wake_relay: Notify,
    node_id: String,
    next_connection_id: AtomicU64,
}

impl RoomManager {
    pub fn new(store: RedisSessionStore) -> Arc<Self> {
        let manager = Arc::new(Self {
            store,
            rooms: RwLock::new(HashMap::new()),
            wake_relay: Notify::new(),
            node_id: format!("{}-{}", std::process::id(), now_nanos()),
            next_connection_id: AtomicU64::new(1),
        });
        manager.start_background_tasks();
        manager
    }

    pub fn store(&self) -> &RedisSessionStore {
        &self.store
    }

    pub fn next_connection_id(&self) -> String {
        format!(
            "{}-{}",
            self.node_id,
            self.next_connection_id.fetch_add(1, Ordering::Relaxed)
        )
    }

    pub async fn join(&self, room_id: &str) -> redis::RedisResult<RoomSubscription> {
        if let Some(room) = self.rooms.read().await.get(room_id).cloned() {
            room.subscribers.fetch_add(1, Ordering::Relaxed);
            return Ok(RoomSubscription {
                room_id: room_id.to_string(),
                receiver: room.sender.subscribe(),
                room,
            });
        }

        let cursor = self.store.latest_event_id(room_id).await?;
        let mut rooms = self.rooms.write().await;
        let room = if let Some(room) = rooms.get(room_id).cloned() {
            room.subscribers.fetch_add(1, Ordering::Relaxed);
            room
        } else {
            let (sender, _) = broadcast::channel(self.store.sync_config().connection_buffer.max(1));
            let room = Arc::new(RelayRoom {
                sender,
                cursor: Mutex::new(cursor),
                subscribers: AtomicUsize::new(1),
            });
            rooms.insert(room_id.to_string(), room.clone());
            self.wake_relay.notify_one();
            room
        };
        Ok(RoomSubscription {
            room_id: room_id.to_string(),
            receiver: room.sender.subscribe(),
            room,
        })
    }

    pub async fn leave(&self, subscription: RoomSubscription) {
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

    pub fn is_ready(&self) -> bool {
        self.store.is_healthy()
    }

    fn start_background_tasks(self: &Arc<Self>) {
        tokio::spawn(self.clone().relay_loop());
        tokio::spawn(self.clone().health_loop());
        tokio::spawn(self.clone().expired_connection_loop());
        tokio::spawn(self.clone().worker_supervisor());
    }

    async fn relay_loop(self: Arc<Self>) {
        let mut connection = loop {
            match self.store.dedicated_connection().await {
                Ok(connection) => break connection,
                Err(error) => {
                    tracing::error!(%error, "failed to connect Redis room relay");
                    tokio::time::sleep(Duration::from_secs(1)).await;
                }
            }
        };
        loop {
            let rooms: Vec<(String, Arc<RelayRoom>)> = self
                .rooms
                .read()
                .await
                .iter()
                .map(|(id, room)| (id.clone(), room.clone()))
                .collect();
            if rooms.is_empty() {
                self.wake_relay.notified().await;
                continue;
            }
            let query = {
                let mut query = Vec::with_capacity(rooms.len());
                for (room_id, room) in &rooms {
                    query.push((room_id.clone(), room.cursor.lock().await.clone()));
                }
                query
            };

            match self.store.read_rooms(&mut connection, &query).await {
                Ok(events_by_room) => {
                    for (room_id, events) in events_by_room {
                        let Some(room) = rooms
                            .iter()
                            .find_map(|(id, room)| (id == &room_id).then_some(room))
                        else {
                            continue;
                        };
                        for event in events {
                            *room.cursor.lock().await = event.id.clone();
                            let _ = room.sender.send(RelayMessage::Event(event));
                        }
                    }
                }
                Err(error) => {
                    tracing::error!(%error, "Redis room relay failed");
                    self.broadcast_redis_failure(&rooms);
                    tokio::time::sleep(Duration::from_secs(1)).await;
                    if let Ok(new_connection) = self.store.dedicated_connection().await {
                        connection = new_connection;
                    }
                }
            }
        }
    }

    fn broadcast_redis_failure(&self, rooms: &[(String, Arc<RelayRoom>)]) {
        for (_, room) in rooms {
            let _ = room.sender.send(RelayMessage::RedisUnavailable);
        }
    }

    async fn health_loop(self: Arc<Self>) {
        let mut interval = tokio::time::interval(Duration::from_secs(2));
        loop {
            interval.tick().await;
            if let Err(error) = self.store.ping().await {
                tracing::warn!(%error, "Redis health check failed");
            }
        }
    }

    async fn expired_connection_loop(self: Arc<Self>) {
        let interval_seconds = self.store.sync_config().lease_heartbeat_seconds.max(1);
        let mut interval = tokio::time::interval(Duration::from_secs(interval_seconds));
        loop {
            interval.tick().await;
            let expired = match self.store.expired_connections(500).await {
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
        for index in 0..self.store.worker_config().concurrency.max(1) {
            tokio::spawn(
                self.clone()
                    .worker_loop(format!("{}-{index}", self.node_id)),
            );
        }
    }

    async fn worker_loop(self: Arc<Self>, consumer: String) {
        let mut claim_interval = tokio::time::interval(Duration::from_secs(
            self.store.worker_config().task_timeout_seconds.max(2) / 2,
        ));
        let mut connection = loop {
            match self.store.dedicated_connection().await {
                Ok(connection) => break connection,
                Err(error) => {
                    tracing::warn!(%error, %consumer, "failed to connect Redis compaction worker");
                    tokio::time::sleep(Duration::from_secs(1)).await;
                }
            }
        };
        loop {
            let tasks = tokio::select! {
                _ = claim_interval.tick() => self.store.claim_worker_tasks(
                    &consumer,
                    1,
                ).await,
                tasks = self.store.read_worker_tasks(
                    &mut connection,
                    &consumer,
                    1,
                ) => tasks,
            };
            let tasks = match tasks {
                Ok(tasks) => tasks,
                Err(error) => {
                    tracing::warn!(%error, %consumer, "Redis compaction worker read failed");
                    tokio::time::sleep(Duration::from_secs(1)).await;
                    match self.store.dedicated_connection().await {
                        Ok(new_connection) => connection = new_connection,
                        Err(_) => continue,
                    }
                    continue;
                }
            };
            for (task_id, room_id) in tasks {
                tokio::time::sleep(Duration::from_secs(
                    self.store.worker_config().task_debounce_seconds,
                ))
                .await;
                match self.store.compact_room(&room_id).await {
                    Ok(()) => {
                        if let Err(error) = self.store.acknowledge_worker_task(&task_id).await {
                            tracing::warn!(%error, %task_id, "failed to acknowledge compaction task");
                        }
                    }
                    Err(error) => {
                        tracing::error!(%error, %room_id, %task_id, "room compaction failed")
                    }
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
