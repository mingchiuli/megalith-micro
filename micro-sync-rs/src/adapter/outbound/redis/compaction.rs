use super::*;

pub(crate) struct RedisCompactionTaskReader {
    connection: MultiplexedConnection,
    worker_stream: String,
    worker_group: String,
    healthy: Arc<AtomicBool>,
}

impl RedisSessionStore {
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

    #[cfg(test)]
    pub(super) async fn read_worker_tasks(
        &self,
        connection: &mut MultiplexedConnection,
        consumer: &str,
        count: usize,
    ) -> RedisResult<Vec<(String, String)>> {
        let mut command = redis::cmd("XREADGROUP");
        command
            .arg("GROUP")
            .arg(self.worker_group())
            .arg(consumer)
            .arg("COUNT")
            .arg(count)
            .arg("BLOCK")
            .arg(WORKER_READ_BLOCK_MILLIS)
            .arg("STREAMS")
            .arg(self.worker_stream())
            .arg(">");
        connection.set_response_timeout(blocking_response_timeout(WORKER_READ_BLOCK_MILLIS));
        let reply = command.query_async::<StreamReadReply>(connection).await?;
        self.healthy.store(true, Ordering::Relaxed);
        Ok(compaction_tasks(reply)
            .into_iter()
            .map(|task| (task.id, task.room_id))
            .collect())
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

        let script = Script::new(scripts::COMPACT_ROOM);
        let mut connection = self.connection.clone();
        let committed = script
            .key(self.snapshot_key(room_id))
            .key(self.pending_key(room_id))
            .key(self.room_stream(room_id))
            .key(self.room_leases_key(room_id))
            .arg(snapshot.revision)
            .arg(format_event_cursor(&highwater))
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
        let script = Script::new(scripts::ACKNOWLEDGE_WORKER_TASK);
        script
            .key(self.worker_stream())
            .arg(self.worker_group())
            .arg(task_id)
            .invoke_async::<i32>(&mut connection)
            .await
            .map(|_| ())
    }

    async fn enqueue_compaction(&self, room_id: &str) -> RedisResult<()> {
        let script = Script::new(scripts::ENQUEUE_COMPACTION);
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
            through: through.map_or_else(EventCursor::default, |value| parse_event_cursor(&value)),
            update: update.unwrap_or_default(),
        })
    }
}

impl CompactionTaskReader for RedisCompactionTaskReader {
    async fn read(&mut self, consumer: &str, count: usize) -> StoreResult<Vec<CompactionTask>> {
        let mut command = redis::cmd("XREADGROUP");
        command
            .arg("GROUP")
            .arg(&self.worker_group)
            .arg(consumer)
            .arg("COUNT")
            .arg(count)
            .arg("BLOCK")
            .arg(WORKER_READ_BLOCK_MILLIS)
            .arg("STREAMS")
            .arg(&self.worker_stream)
            .arg(">");
        self.connection
            .set_response_timeout(blocking_response_timeout(WORKER_READ_BLOCK_MILLIS));
        match command
            .query_async::<StreamReadReply>(&mut self.connection)
            .await
        {
            Ok(reply) => {
                self.healthy.store(true, Ordering::Relaxed);
                Ok(compaction_tasks(reply))
            }
            Err(error) => {
                self.healthy.store(false, Ordering::Relaxed);
                Err(store_error(error))
            }
        }
    }
}

impl CompactionStore for RedisSessionStore {
    type Reader = RedisCompactionTaskReader;

    async fn task_reader(&self) -> StoreResult<Self::Reader> {
        let connection = self.dedicated_connection().await.map_err(store_error)?;
        Ok(RedisCompactionTaskReader {
            connection,
            worker_stream: self.worker_stream(),
            worker_group: self.worker_group(),
            healthy: Arc::clone(&self.healthy),
        })
    }

    async fn claim_tasks(&self, consumer: &str, count: usize) -> StoreResult<Vec<CompactionTask>> {
        self.claim_worker_tasks(consumer, count)
            .await
            .map(|tasks| {
                tasks
                    .into_iter()
                    .map(|(id, room_id)| CompactionTask { id, room_id })
                    .collect()
            })
            .map_err(store_error)
    }

    async fn compact_room(&self, room_id: &str) -> StoreResult<()> {
        RedisSessionStore::compact_room(self, room_id)
            .await
            .map_err(store_error)
    }

    async fn acknowledge_task(&self, task_id: &str) -> StoreResult<()> {
        self.acknowledge_worker_task(task_id)
            .await
            .map_err(store_error)
    }
}

fn compaction_tasks(reply: StreamReadReply) -> Vec<CompactionTask> {
    reply
        .keys
        .into_iter()
        .flat_map(|stream| stream.ids)
        .filter_map(|mut entry| {
            take_field::<String>(&mut entry.map, "room").map(|room_id| CompactionTask {
                id: entry.id,
                room_id,
            })
        })
        .collect()
}
