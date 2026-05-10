use std::{collections::HashMap, usize};

use axum::{
    BoxError,
    body::{self, Bytes},
    extract::Request,
    http::{HeaderName, HeaderValue, request::Builder},
};
use http_body_util::{BodyExt, combinators::BoxBody};
use http_body_util::{Empty, Full};
use hyper::{
    Method, Response, Uri,
    body::Buf,
    client::conn::http1::{self, SendRequest},
    header,
};
use hyper_util::rt::TokioIo;
use opentelemetry::global;
use opentelemetry_http::HeaderInjector;
use serde::{Serialize, de::DeserializeOwned};
use tokio::net::TcpStream;
use tracing_opentelemetry::OpenTelemetrySpanExt;

use crate::{exception::ClientError, utils::set_headers};

use serde::Deserialize;

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AuthRouteReq {
    method: String,
    route_mapping: String,
    ip_addr: String,
}

impl AuthRouteReq {
    pub fn new(method: &Method, route_mapping: String, ip_addr: String) -> Self {
        AuthRouteReq {
            method: method.to_string(),
            route_mapping,
            ip_addr,
        }
    }
}

#[derive(Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct AuthRouteResp {
    service_host: String,
    service_port: u32,
}

impl AuthRouteResp {
    pub fn service_host(&self) -> &str {
        &self.service_host
    }

    pub fn service_port(&self) -> &u32 {
        &self.service_port
    }
}

async fn parse_req(
    url: hyper::Uri,
    method: Method,
    mut headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Builder, ClientError> {
    // 注入 Trace Context
    inject_trace_context_hashmap(&mut headers);

    let host = parse_host(&url)?;

    let mut req = Request::builder()
        .method(method)
        .uri(url)
        .header(header::HOST, host);
    req = set_headers(req, headers);
    Ok(req)
}

/// 注入 Trace Context 到 HashMap headers
fn inject_trace_context_hashmap(headers: &mut HashMap<HeaderName, HeaderValue>) {
    use hyper::HeaderMap;
    use std::iter::FromIterator;

    // 1. 将 HashMap 转换为 hyper::HeaderMap
    let mut hyper_headers: HeaderMap<HeaderValue> = HeaderMap::from_iter(headers.drain());

    // 2. 注入 Trace Context
    global::get_text_map_propagator(|propagator| {
        let current_context = tracing::Span::current().context();
        propagator.inject_context(&current_context, &mut HeaderInjector(&mut hyper_headers));
    });

    // 3. 转换回 HashMap
    *headers = hyper_headers
        .into_iter()
        .filter_map(|(opt_name, value)| opt_name.map(|name| (name, value)))
        .collect();
}

pub async fn get_raw(
    url: Uri,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<BoxBody<Bytes, BoxError>>, BoxError> {
    request_raw(Method::GET, url, None, headers).await
}

pub async fn post_raw(
    url: Uri,
    body: axum::body::Body,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<BoxBody<Bytes, BoxError>>, BoxError> {
    request_raw(Method::POST, url, Some(body), headers).await
}

//这个方法阻塞响应的
pub async fn post<T: DeserializeOwned>(
    url: Uri,
    body: impl Serialize,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<T, BoxError> {
    request(Method::POST, url, Some(body), headers).await
}

fn parse_host(url: &Uri) -> Result<HeaderValue, ClientError> {
    let host = url
        .authority()
        .ok_or(ClientError::Request("No authority found".to_string()))?
        .as_str();

    HeaderValue::from_str(host).map_err(|_| ClientError::Request("Invalid host".to_string()))
}

async fn parse_response<T>(response: Response<BoxBody<Bytes, BoxError>>) -> Result<T, BoxError>
where
    T: DeserializeOwned,
{
    let status = response.status();

    // 从流式 body 收集完整数据
    let body = response.into_body().collect().await?.to_bytes();

    // 检查状态码（可选但推荐）
    if !status.is_success() {
        return Err(Box::new(ClientError::Status(
            status.as_u16(),
            String::from_utf8_lossy(&body).to_string(),
        )));
    }

    Ok(serde_json::from_reader(body.reader())
        .map_err(|e| ClientError::Deserialize(e.to_string()))?)
}

pub async fn request<T: DeserializeOwned>(
    method: Method,
    url: Uri,
    body: Option<impl Serialize>,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<T, BoxError> {
    let body = match body {
        Some(b) => {
            let json = serde_json::to_string(&b)
                .map_err(|e| Box::new(ClientError::Serialization(e.to_string())) as BoxError)?;
            Some(axum::body::Body::from(json))
        },
        None => None,
    };

    let response = request_raw(method, url, body, headers).await?;
    parse_response(response).await
}

pub async fn request_raw(
    method: Method,
    url: hyper::Uri,
    body: Option<axum::body::Body>,
    headers: HashMap<HeaderName, HeaderValue>,
) -> Result<Response<BoxBody<Bytes, BoxError>>, BoxError> {
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

    invoke(sender, req).await
}

async fn create_connection<B>(url: &hyper::Uri) -> Result<SendRequest<B>, BoxError>
where
    B: hyper::body::Body<Data = Bytes, Error = BoxError> + Send + 'static,
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
            tracing::error!("Connection error: {}", e);
        }
    });

    Ok(sender)
}

// 流式 invoke - 修复版
async fn invoke<B>(
    mut sender: SendRequest<B>,
    req: Request<B>,
) -> Result<Response<BoxBody<Bytes, BoxError>>, BoxError>
where
    B: hyper::body::Body + Send + 'static,
    B::Data: Send,
    B::Error: Into<BoxError>,
{
    let res = sender.send_request(req).await?;

    // ✅ 不要过滤状态码，让所有响应都能正常转发
    // 将 body 转换为 BoxBody 并返回完整的响应
    let (parts, body) = res.into_parts();

    let boxed_body = body
        .map_err(|e| Box::new(ClientError::Network(e.to_string())) as BoxError)
        .boxed();

    Ok(Response::from_parts(parts, boxed_body))
}
