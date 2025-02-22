use std::{collections::HashMap, usize};

use axum::{
    body::{self, Bytes},
    extract::Request,
    http::{request::Builder, HeaderName, HeaderValue},
    BoxError,
};
use http_body_util::{combinators::BoxBody, BodyExt};
use http_body_util::{Empty, Full};
use hyper::{
    body::{Body, Buf},
    client::conn::http1::{self, SendRequest},
    Method, Response,
};
use hyper_util::rt::TokioIo;
use serde::{de::DeserializeOwned, Serialize};
use tokio::net::TcpStream;

use crate::{exception::error::ClientError, util::http_util::set_headers};

async fn parse_req(
    url: hyper::Uri,
    method: Method,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Builder, ClientError> {
    let host = parse_host(&url)?;

    let mut req = Request::builder()
        .method(method)
        .uri(url)
        .header(hyper::header::HOST, host);
    req = set_headers(req, headers);
    Ok(req)
}

// Raw GET 方法
pub async fn get_raw(
    url: hyper::Uri,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<Bytes>, BoxError> {
    request_raw(Method::GET, url, None, headers).await
}

// Raw POST 方法
pub async fn post_raw(
    url: hyper::Uri,
    body: axum::body::Body,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<Bytes>, BoxError> {
    request_raw(Method::POST, url, Some(body), headers).await
}

pub async fn get<T: DeserializeOwned>(
    url: hyper::Uri,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<T, BoxError> {
    request(Method::GET, url, None::<()>, headers).await
}

pub async fn post<T: DeserializeOwned>(
    url: hyper::Uri,
    body: impl Serialize,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<T, BoxError> {
    request(Method::POST, url, Some(body), headers).await
}

fn parse_host(url: &hyper::Uri) -> Result<HeaderValue, ClientError> {
    let host = url
        .authority()
        .ok_or(ClientError::Request("No authority found".to_string()))?
        .as_str();

    HeaderValue::from_str(host).map_err(|_| ClientError::Request("Invalid host".to_string()))
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
        .map_err(|e| ClientError::Deserialize(e.to_string()))?)
}

pub async fn request<T: DeserializeOwned>(
    method: Method,
    url: hyper::Uri,
    body: Option<impl Serialize>,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<T, BoxError> {
    let body = body.map(|b| {
        let json = serde_json::to_string(&b)
            .map_err(|e| ClientError::Serialization(e.to_string()))
            .unwrap();
        axum::body::Body::from(json)
    });

    let response = request_raw(method, url, body, headers).await?;
    parse_response(response).await
}

pub async fn request_raw(
    method: Method,
    url: hyper::Uri,
    body: Option<axum::body::Body>,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<Bytes>, BoxError> {
    let sender = create_connection::<BoxBody<Bytes, BoxError>>(&url).await?;
    let req = parse_req(url, method, headers).await?;

    let req = match body {
        Some(body) => {
            let body_bytes = body::to_bytes(body.into(), usize::MAX).await?;
            let body = Full::new(body_bytes)
                .map_err(|e| Box::new(ClientError::Request(e.to_string())) as BoxError)
                .boxed();
            req.body(body)
        }
        None => {
            let body = Empty::<Bytes>::new()
                .map_err(|e| Box::new(ClientError::Request(e.to_string())) as BoxError)
                .boxed();
            req.body(body)
        }
    }
    .map_err(|e| ClientError::Request(e.to_string()))?;

    let res_body = invoke(sender, req).await?;
    Ok(Response::new(res_body))
}

async fn create_connection<B>(url: &hyper::Uri) -> Result<SendRequest<B>, BoxError>
where
    B: Body<Data = Bytes, Error = BoxError> + Send + 'static,
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

async fn invoke<B>(mut sender: SendRequest<B>, req: Request<B>) -> Result<Bytes, BoxError>
where
    B: Body + Send + 'static,
    B::Data: Send,
    B::Error: Into<BoxError>,
{
    let res = sender.send_request(req).await?;
    let body = res.into_body().collect().await?.to_bytes();
    Ok(body)
}
