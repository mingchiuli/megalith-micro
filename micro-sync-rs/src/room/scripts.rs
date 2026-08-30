pub(super) const LOAD_ROOM: &str = include_str!("scripts/load-room.lua");
pub(super) const APPEND_EVENT: &str = include_str!("scripts/append-event.lua");
pub(super) const REGISTER_CONNECTION: &str = include_str!("scripts/register-connection.lua");
pub(super) const UPDATE_AWARENESS: &str = include_str!("scripts/update-awareness.lua");
pub(super) const DISCONNECT: &str = include_str!("scripts/disconnect.lua");
pub(super) const EXPIRED_CONNECTIONS: &str = include_str!("scripts/expired-connections.lua");
pub(super) const COMPACT_ROOM: &str = include_str!("scripts/compact-room.lua");
pub(super) const ACKNOWLEDGE_WORKER_TASK: &str =
    include_str!("scripts/acknowledge-worker-task.lua");
pub(super) const ENQUEUE_COMPACTION: &str = include_str!("scripts/enqueue-compaction.lua");
