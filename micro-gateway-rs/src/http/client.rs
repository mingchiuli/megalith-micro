use std::{collections::HashMap, usize};

use axum::{
    body::{self, Body, Bytes},
    extract::Request,
    http::{HeaderName, HeaderValue},
    BoxError,
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

use crate::{exception::error::ClientError, util::http_util::set_headers};

/// Helper function to create a new client connection

async fn create_connection<B>(url: &hyper::Uri) -> Result<SendRequest<B>, BoxError>
where
    B: hyper::body::Body + Send + 'static,
    B::Data: Send,
    B::Error: Into<BoxError>,
{
    let host = url
        .host()
        .ok_or(ClientError::Request("No host found".to_string()))?;
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
) -> Result<Response<Bytes>, BoxError> {
    let mut sender = create_connection::<Empty<Bytes>>(url).await?;

    let mut builder = Request::builder().method(Method::GET).uri(url).header(
        hyper::header::HOST,
        url.authority()
            .ok_or(ClientError::Request("No authority found".to_string()))?
            .as_str(),
    );

    builder = set_headers(builder, headers);

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
) -> Result<T, BoxError> {
    let resp = get_raw(url, headers).await?;
    let data: T = parse_response(resp).await?;
    Ok(data)
}

pub async fn post_raw(
    url: &hyper::Uri,
    req_body: Body,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<Bytes>, BoxError> {
    let mut sender = create_connection::<Full<Bytes>>(url).await?;

    let mut builder = Request::builder().method(Method::POST).uri(url).header(
        hyper::header::HOST,
        url.authority()
            .ok_or(ClientError::Request("No authority found".to_string()))?
            .as_str(),
    );

    builder = set_headers(builder, headers);

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
) -> Result<T, BoxError> {
    // Serialize request body and set content type
    let req_body =
        serde_json::to_string(&req_body).map_err(|e| ClientError::Serialization(e.to_string()))?;

    // Use post_raw to handle the request
    let response = post_raw(url, Body::from(req_body), headers).await?;

    // Parse the response
    parse_response(response).await
}

async fn parse_response<T>(response: Response<Bytes>) -> Result<T, BoxError>
where
    T: DeserializeOwned,
{
    let status = response.status();
    let body = response.into_body();

    // Check if status is success (2xx range)
    if !status.is_success() {
        return Err(Box::new(ClientError::Api(format!(
            "Request failed with status: {}",
            status
        ))));
    }

    // Try to deserialize the response body
    Ok(serde_json::from_reader(body.reader())
        .map_err(|e| Box::new(ClientError::Deserialize(e.to_string())))?)
}
