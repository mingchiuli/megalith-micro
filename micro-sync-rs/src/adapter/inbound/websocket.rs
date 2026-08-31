use std::sync::Arc;

use axum::extract::ws::{CloseFrame, Message, WebSocket, WebSocketUpgrade};
use axum::extract::{Path, State};
use axum::http::HeaderMap;
use axum::response::IntoResponse;
use futures_util::StreamExt;
use opentelemetry::global;
use tokio::sync::broadcast;
use tracing::Instrument;
use tracing_opentelemetry::OpenTelemetrySpanExt;

use crate::application::error::StoreResult;
use crate::application::port::CollaborationStore;
use crate::application::room_manager::{RelayMessage, RoomManager, RoomSubscription};
use crate::domain::protocol::{
    ClientMessage, decode_client_message, encode_awareness, initial_messages,
    merge_awareness_updates, sync_step2,
};
use crate::extractor::AxumHeaderExtractor;

const CLOSE_PROTOCOL_ERROR: u16 = 1002;
const CLOSE_TRY_AGAIN_LATER: u16 = 1013;

pub(crate) async fn ws_handler<S>(
    Path(room_id): Path<String>,
    State(room_manager): State<Arc<RoomManager<S>>>,
    headers: HeaderMap,
    ws: WebSocketUpgrade,
) -> impl IntoResponse
where
    S: CollaborationStore,
{
    let parent_context = global::get_text_map_propagator(|propagator| {
        propagator.extract(&AxumHeaderExtractor(&headers))
    });
    let span = tracing::info_span!(
        "websocket_connection",
        room_id = %room_id,
        otel.name = format!("WebSocket: /rooms/{room_id}")
    );
    let _ = span.set_parent(parent_context);
    ws.on_upgrade(move |socket| peer(socket, room_manager, room_id).instrument(span))
}

async fn peer<S>(mut socket: WebSocket, room_manager: Arc<RoomManager<S>>, room_id: String)
where
    S: CollaborationStore,
{
    let requested_room_id: Arc<str> = room_id.into();
    let connection_id = room_manager.next_connection_id();
    let mut subscription = match room_manager.join(requested_room_id).await {
        Ok(subscription) => subscription,
        Err(error) => {
            tracing::error!(%error, "failed to subscribe room relay");
            close(&mut socket, CLOSE_TRY_AGAIN_LATER, "Redis unavailable").await;
            return;
        }
    };
    let room_id = subscription.shared_room_id();
    if let Err(error) = room_manager
        .register_connection(&room_id, &connection_id)
        .await
    {
        tracing::error!(%error, %room_id, %connection_id, "failed to register collaboration connection");
        close(&mut socket, CLOSE_TRY_AGAIN_LATER, "Redis unavailable").await;
        room_manager.leave(subscription).await;
        return;
    }

    tracing::info!(%room_id, %connection_id, "collaboration connection established");
    if let Err(error) = run_session(
        &mut socket,
        &room_manager,
        &room_id,
        &connection_id,
        &mut subscription,
    )
    .await
    {
        tracing::warn!(%error, %room_id, %connection_id, "collaboration connection ended");
    }

    if let Err(error) = room_manager.disconnect(&room_id, &connection_id).await {
        tracing::warn!(%error, %room_id, %connection_id, "failed to clean up collaboration connection");
    }
    room_manager.leave(subscription).await;
}

async fn run_session<S>(
    socket: &mut WebSocket,
    room_manager: &RoomManager<S>,
    room_id: &str,
    connection_id: &str,
    subscription: &mut RoomSubscription,
) -> Result<(), String>
where
    S: CollaborationStore,
{
    let state = match tokio::time::timeout(
        room_manager.initial_sync_timeout(),
        room_manager.load_room(room_id),
    )
    .await
    {
        Ok(Ok(state)) => state,
        Ok(Err(error)) => {
            close(socket, CLOSE_TRY_AGAIN_LATER, "Redis unavailable").await;
            return Err(error.to_string());
        }
        Err(_) => {
            close(socket, CLOSE_TRY_AGAIN_LATER, "initial sync timed out").await;
            return Err("initial sync timed out".to_string());
        }
    };
    for message in initial_messages(&state.document, &state.awareness)? {
        socket
            .send(Message::Binary(message))
            .await
            .map_err(|error| error.to_string())?;
    }

    let initial_highwater = state.highwater;
    let mut heartbeat = tokio::time::interval(room_manager.lease_heartbeat());
    heartbeat.set_missed_tick_behavior(tokio::time::MissedTickBehavior::Delay);
    heartbeat.tick().await;

    loop {
        tokio::select! {
            incoming = socket.next() => {
                match incoming {
                    Some(Ok(Message::Binary(data))) => {
                        let message = match decode_client_message(&data) {
                            Ok(message) => message,
                            Err(error) => {
                                close(socket, CLOSE_PROTOCOL_ERROR, error).await;
                                return Err(error.to_string());
                            }
                        };
                        match message {
                            ClientMessage::SyncStep1(state_vector) => {
                                let state = store_or_close(socket, room_manager.load_room(room_id).await).await?;
                                let response = sync_step2(&state.document, state_vector)?;
                                socket.send(Message::Binary(response)).await.map_err(|error| error.to_string())?;
                            }
                            ClientMessage::DocumentUpdate(update) => {
                                store_or_close(
                                    socket,
                                    room_manager.append_document_update(room_id, connection_id, update).await,
                                ).await?;
                            }
                            ClientMessage::Awareness(awareness) => {
                                for client in awareness.clients {
                                    store_or_close(
                                        socket,
                                        room_manager.update_awareness(
                                            room_id,
                                            connection_id,
                                            &client,
                                        ).await,
                                    ).await?;
                                }
                            }
                            ClientMessage::QueryAwareness => {
                                let updates = store_or_close(socket, room_manager.read_awareness(room_id).await).await?;
                                let update = merge_awareness_updates(&updates)?;
                                socket.send(Message::Binary(encode_awareness(&update))).await.map_err(|error| error.to_string())?;
                            }
                            ClientMessage::Ignored => {}
                        }
                    }
                    Some(Ok(Message::Ping(data))) => {
                        socket.send(Message::Pong(data)).await.map_err(|error| error.to_string())?;
                    }
                    Some(Ok(Message::Close(_))) | None => return Ok(()),
                    Some(Ok(_)) => {}
                    Some(Err(error)) => return Err(error.to_string()),
                }
            }
            relay = subscription.receiver.recv() => {
                match relay {
                    Ok(RelayMessage::Event(event)) => {
                        if !event.should_forward_to(connection_id, &initial_highwater) {
                            continue;
                        }
                        socket.send(Message::Binary(event.frame.clone())).await.map_err(|error| error.to_string())?;
                    }
                    Ok(RelayMessage::StoreUnavailable) => {
                        close(socket, CLOSE_TRY_AGAIN_LATER, "Redis unavailable").await;
                        return Err("Redis room relay unavailable".to_string());
                    }
                    Err(broadcast::error::RecvError::Lagged(count)) => {
                        tracing::warn!(%room_id, %connection_id, count, "collaboration connection buffer lagged");
                        close(socket, CLOSE_TRY_AGAIN_LATER, "sync buffer exceeded").await;
                        return Err(format!("relay lagged by {count} events"));
                    }
                    Err(broadcast::error::RecvError::Closed) => return Ok(()),
                }
            }
            _ = heartbeat.tick() => {
                store_or_close(
                    socket,
                    room_manager.refresh_connection(room_id, connection_id).await,
                ).await?;
            }
        }
    }
}

async fn store_or_close<T>(socket: &mut WebSocket, result: StoreResult<T>) -> Result<T, String> {
    match result {
        Ok(value) => Ok(value),
        Err(error) => {
            close(socket, CLOSE_TRY_AGAIN_LATER, "Redis unavailable").await;
            Err(error.to_string())
        }
    }
}

async fn close(socket: &mut WebSocket, code: u16, reason: &'static str) {
    let _ = socket
        .send(Message::Close(Some(CloseFrame {
            code,
            reason: reason.into(),
        })))
        .await;
}
