use super::*;

impl RedisSessionStore {
    pub async fn register_connection(&self, room_id: &str, connection_id: &str) -> RedisResult<()> {
        let script = Script::new(scripts::REGISTER_CONNECTION);
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
        let script = Script::new(scripts::UPDATE_AWARENESS);
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
        let script = Script::new(scripts::DISCONNECT);
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
        let script = Script::new(scripts::EXPIRED_CONNECTIONS);
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
}
