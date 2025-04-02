use futures_util::StreamExt;
use std::sync::Arc;
use tokio::sync::Mutex;
use warp::ws::{WebSocket, Ws};
use warp::{Rejection, Reply};
use yrs_warp::ws::{WarpSink, WarpStream};

use crate::room::room_info::{RoomConnection, RoomManager};

pub async fn ws_handler(
    room_id: String,
    ws: Ws,
    room_manager: Arc<Mutex<RoomManager>>,
) -> Result<impl Reply, Rejection> {
    log::info!("新连接尝试加入房间: {}", room_id);
    let room_id_clone = room_id.clone();

    Ok(ws.on_upgrade(move |socket| peer(socket, room_manager, room_id_clone)))
}

async fn peer(ws: WebSocket, room_manager: Arc<Mutex<RoomManager>>, room_id: String) {
    log::info!("已建立新连接到房间: {}", room_id);

    // 获取或创建此房间的广播组，同时获取房间信息引用
    let (room_info, bcast) = {
        let mut manager = room_manager.lock().await;
        manager.get_or_create_room(&room_id).await
    };

    // 创建连接信息对象，用于管理生命周期
    let connection = RoomConnection::new(room_id.clone(), room_info.clone(), room_manager.clone());

    let (sink, stream) = ws.split();
    let sink = Arc::new(Mutex::new(WarpSink::from(sink)));
    let stream = WarpStream::from(stream);
    let sub = bcast.subscribe(sink, stream);

    // 等待连接关闭
    match sub.completed().await {
        Ok(_) => log::info!("房间 {} 的通道广播成功完成", room_id),
        Err(e) => log::error!("房间 {} 的通道广播异常终止: {}", room_id, e),
    }

    // 连接关闭时清理资源
    connection.cleanup().await;
}
