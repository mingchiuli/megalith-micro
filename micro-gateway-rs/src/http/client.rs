use std::{collections::HashMap, usize};

use axum::{
    body::{self, Body, Bytes},
    extract::Request,
    http::{HeaderName, HeaderValue},
};
use http_body_util::BodyExt;
use http_body_util::{Empty, Full};
use hyper::{
    body::Buf,
    client::conn::http1::{self, SendRequest},
    Method, Response,
};
use hyper_util::rt::TokioIo;
use serde::{de::DeserializeOwned, Serialize};
use tokio::net::TcpStream;

use crate::util::http_util::{set_headers, ClientError, Result};

/// Helper function to create a new client connection
async fn create_connection_with_empty_body(url: &hyper::Uri) -> Result<SendRequest<Empty<Bytes>>> {
    let host = url
        .host()
        .ok_or(ClientError::Network("No host found".to_string()))?;
    let port = url.port_u16().unwrap_or(8080);

    let stream = TcpStream::connect(format!("{}:{}", host, port))
        .await
        .map_err(|e| ClientError::Network(e.to_string()))?;
    let io = TokioIo::new(stream);

    let (sender, conn) = http1::handshake(io)
        .await
        .map_err(|e| ClientError::Network(e.to_string()))?;

    tokio::task::spawn(async move {
        if let Err(e) = conn.await {
            log::error!("Connection error: {}", e);
        }
    });

    Ok(sender)
}

async fn create_connection_with_full_body(url: &hyper::Uri) -> Result<SendRequest<Full<Bytes>>> {
    let host = url
        .host()
        .ok_or(ClientError::Network("No host found".to_string()))?;
    let port = url.port_u16().unwrap_or(8080);

    let stream = TcpStream::connect(format!("{}:{}", host, port))
        .await
        .map_err(|e| ClientError::Network(e.to_string()))?;
    let io = TokioIo::new(stream);

    let (sender, conn) = http1::handshake(io)
        .await
        .map_err(|e| ClientError::Network(e.to_string()))?;

    tokio::task::spawn(async move {
        if let Err(e) = conn.await {
            log::error!("Connection error: {}", e);
        }
    });

    Ok(sender)
}

pub async fn get_raw(
    url: &hyper::Uri,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<Bytes>> {
    let mut sender = create_connection_with_empty_body(&url).await?;

    let mut builder = Request::builder()
        .method(Method::GET)
        .uri(url)
        .header(hyper::header::CONTENT_TYPE, "application/json");

    builder = set_headers(builder, headers)?;

    let req = builder
        .body(Empty::<Bytes>::new())
        .map_err(|e| ClientError::Request(e.to_string()))?;

    let web_res = sender.send_request(req).await?;

    let res_body = web_res.into_body().collect().await?.to_bytes();

    Ok(Response::new(res_body))
}

pub async fn get<T: DeserializeOwned>(
    url: &hyper::Uri,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<T> {
    let mut sender = create_connection_with_empty_body(&url).await?;

    let mut builder = Request::builder()
        .method(Method::GET)
        .uri(url.clone())
        .header(
            hyper::header::HOST,
            url.authority()
                .ok_or(ClientError::Request("No authority found".to_string()))?
                .as_str(),
        )
        .header(hyper::header::ACCEPT, "application/json");

    builder = set_headers(builder, headers)?;

    let req = builder
        .body(Empty::<Bytes>::new())
        .map_err(|e| ClientError::Request(e.to_string()))?;

    let response = sender
        .send_request(req)
        .await
        .map_err(|e| ClientError::Network(e.to_string()))?;

    // Check status code
    let status = response.status();
    if !status.is_success() {
        return Err(Box::new(ClientError::Status(
            status.as_u16(),
            format!("Request failed with status: {}", status),
        )));
    }

    let body = response
        .collect()
        .await
        .map_err(|e| ClientError::Network(e.to_string()))?
        .aggregate();

    Ok(serde_json::from_reader(body.reader())
        .map_err(|e| Box::new(ClientError::Serialization(e.to_string())))?)
}

pub async fn post_raw(
    url: &hyper::Uri,
    req_body: Body,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<Bytes>> {
    let mut sender = create_connection_with_full_body(&url).await?;

    let mut builder = Request::builder()
        .method(Method::POST)
        .uri(url)
        .header(hyper::header::CONTENT_TYPE, "application/json");

    builder = set_headers(builder, headers)?;

    let body_bytes = body::to_bytes(req_body.into(), usize::MAX).await?;

    let req = builder
        .body(Full::new(Bytes::from(body_bytes)))
        .map_err(|e| ClientError::Request(e.to_string()))?;

    let web_res = sender.send_request(req).await?;

    let res_body = web_res.into_body().collect().await?.to_bytes();

    Ok(Response::new(res_body))
}

pub async fn post<T: DeserializeOwned>(
    url: &hyper::Uri,
    req_body: impl Serialize,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<T> {
    let mut sender = create_connection_with_full_body(&url).await?;

    let req_body =
        serde_json::to_string(&req_body).map_err(|e| ClientError::Serialization(e.to_string()))?;

    let mut builder = Request::builder()
        .method(Method::POST)
        .uri(url.clone())
        .header(
            hyper::header::HOST,
            url.authority()
                .ok_or(ClientError::Request("No authority found".to_string()))?
                .as_str(),
        )
        .header(hyper::header::CONTENT_TYPE, "application/json");

    // Add custom headers
    builder = set_headers(builder, headers)?;

    let req = builder
        .body(Full::new(Bytes::from(req_body)))
        .map_err(|e| ClientError::Request(e.to_string()))?;

    log::info!("req:{:?}", req);
    let response = sender
        .send_request(req)
        .await
        .map_err(|e| ClientError::Network(e.to_string()))?;

    // Check status code
    let status = response.status();
    if !status.is_success() {
        log::error!("error client :{:?}", response);
        return Err(Box::new(ClientError::Status(
            status.as_u16(),
            format!("Request failed with status: {}", status),
        )));
    }

    let body = response
        .collect()
        .await
        .map_err(|e| ClientError::Network(e.to_string()))?
        .aggregate();

    Ok(serde_json::from_reader(body.reader())
        .map_err(|e| Box::new(ClientError::Serialization(e.to_string())))?)
}
