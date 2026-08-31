use axum::extract::ws::{CloseFrame, Message as AxumMessage, Utf8Bytes};
use tokio_tungstenite::tungstenite::{Message as TungsteniteMessage, protocol};

pub(crate) fn to_tungstenite_message(message: AxumMessage) -> TungsteniteMessage {
    match message {
        AxumMessage::Text(text) => TungsteniteMessage::text(text.as_str()),
        AxumMessage::Binary(data) => TungsteniteMessage::binary(data.to_vec()),
        AxumMessage::Ping(data) => TungsteniteMessage::Ping(data),
        AxumMessage::Pong(data) => TungsteniteMessage::Pong(data),
        AxumMessage::Close(Some(frame)) => TungsteniteMessage::Close(Some(protocol::CloseFrame {
            code: protocol::frame::coding::CloseCode::from(frame.code),
            reason: frame.reason.as_str().to_string().into(),
        })),
        AxumMessage::Close(None) => TungsteniteMessage::Close(None),
    }
}

pub(crate) fn to_axum_message(message: TungsteniteMessage) -> Option<AxumMessage> {
    match message {
        TungsteniteMessage::Text(text) => Some(AxumMessage::Text(Utf8Bytes::from(text.as_str()))),
        TungsteniteMessage::Binary(data) => Some(AxumMessage::Binary(data)),
        TungsteniteMessage::Ping(data) => Some(AxumMessage::Ping(data)),
        TungsteniteMessage::Pong(data) => Some(AxumMessage::Pong(data)),
        TungsteniteMessage::Close(frame) => {
            Some(AxumMessage::Close(frame.map(|frame| CloseFrame {
                code: frame.code.into(),
                reason: frame.reason.to_string().into(),
            })))
        }
        TungsteniteMessage::Frame(_) => None,
    }
}

#[cfg(test)]
mod tests {
    use bytes::Bytes;

    use super::*;

    #[test]
    fn converts_data_and_control_frames_in_both_directions() {
        let messages = [
            AxumMessage::Text("hello".into()),
            AxumMessage::Binary(Bytes::from_static(b"bytes")),
            AxumMessage::Ping(Bytes::from_static(b"ping")),
            AxumMessage::Pong(Bytes::from_static(b"pong")),
        ];

        for message in messages {
            assert!(to_axum_message(to_tungstenite_message(message)).is_some());
        }
    }

    #[test]
    fn preserves_close_code_and_reason() {
        let converted = to_axum_message(to_tungstenite_message(AxumMessage::Close(Some(
            CloseFrame {
                code: 1001,
                reason: "leaving".into(),
            },
        ))))
        .unwrap();

        let AxumMessage::Close(Some(frame)) = converted else {
            panic!("expected close frame");
        };
        assert_eq!(frame.code, 1001);
        assert_eq!(frame.reason, "leaving");
    }
}
