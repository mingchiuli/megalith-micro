use anyhow::{Context, Result};
use base64::{Engine as _, engine::general_purpose::STANDARD};
use hmac::{Hmac, Mac};
use lettre::{
    AsyncSmtpTransport, AsyncTransport, Message, Tokio1Executor,
    transport::smtp::authentication::Credentials,
};
use megalith_service_runtime::settings::Settings;
use std::collections::BTreeMap;
use uuid::Uuid;

#[derive(Clone)]
pub struct Delivery {
    pub http: reqwest::Client,
    pub smtp: AsyncSmtpTransport<Tokio1Executor>,
    pub from: String,
    sms_url: String,
    access_key: String,
    access_secret: String,
}
impl Delivery {
    pub fn new(s: &Settings, http: reqwest::Client) -> Result<Self> {
        let host = s.get("spring.mail.host")?;
        let smtp = if s
            .optional("spring.mail.properties.mail.smtp.ssl.enable")
            .as_deref()
            .unwrap_or("true")
            == "true"
        {
            AsyncSmtpTransport::<Tokio1Executor>::relay(&host)?
        } else if s
            .optional("spring.mail.properties.mail.smtp.starttls.enable")
            .as_deref()
            == Some("true")
        {
            AsyncSmtpTransport::<Tokio1Executor>::starttls_relay(&host)?
        } else {
            AsyncSmtpTransport::<Tokio1Executor>::builder_dangerous(&host)
        };
        let mut smtp = smtp.port(s.parse("spring.mail.port")?);
        if let Some(user) = s.optional("spring.mail.username") {
            smtp = smtp.credentials(Credentials::new(user, s.get("spring.mail.password")?));
        }
        Ok(Self {
            http,
            smtp: smtp.build(),
            from: s
                .optional("spring.mail.properties.from")
                .unwrap_or_default(),
            sms_url: s.get("megalith.blog.sms.base-url")?,
            access_key: s
                .optional("megalith.blog.aliyun.access-key-id")
                .unwrap_or_default(),
            access_secret: s
                .optional("megalith.blog.aliyun.access-key-secret")
                .unwrap_or_default(),
        })
    }
}

#[async_trait::async_trait]
impl crate::application::ports::Delivery for Delivery {
    async fn email(&self, to: &str, code: &str) -> Result<()> {
        let message = Message::builder()
            .from(self.from.parse()?)
            .to(to.parse()?)
            .subject("Login Code")
            .body(code.to_owned())?;
        self.smtp
            .send(message)
            .await
            .context("mail delivery failed")?;
        Ok(())
    }
    async fn sms(&self, phone: &str, code: &str) -> Result<()> {
        let timestamp = chrono::Utc::now().format("%Y-%m-%dT%H:%M:%SZ").to_string();
        let nonce = Uuid::new_v4().to_string();
        let param = serde_json::json!({"code":code}).to_string();
        let params = BTreeMap::from([
            ("SignatureMethod", "HMAC-SHA1"),
            ("SignatureNonce", &nonce),
            ("AccessKeyId", &self.access_key),
            ("SignatureVersion", "1.0"),
            ("Timestamp", &timestamp),
            ("Format", "XML"),
            ("Action", "SendSms"),
            ("Version", "2017-05-25"),
            ("RegionId", "cn-hangzhou"),
            ("PhoneNumbers", phone),
            ("SignName", "阿里云短信测试"),
            ("TemplateParam", &param),
            ("TemplateCode", "SMS_154950909"),
        ]);
        let query = params
            .into_iter()
            .map(|(k, v)| format!("{}={}", escape(k), escape(v)))
            .collect::<Vec<_>>()
            .join("&");
        let mut mac =
            Hmac::<sha1::Sha1>::new_from_slice(format!("{}&", self.access_secret).as_bytes())?;
        mac.update(format!("GET&%2F&{}", escape(&query)).as_bytes());
        let signature = escape(&STANDARD.encode(mac.finalize().into_bytes()));
        self.http
            .get(format!(
                "{}/?Signature={signature}&{query}",
                self.sms_url.trim_end_matches('/')
            ))
            .headers(megalith_otel::trace_headers())
            .send()
            .await
            .map_err(reqwest::Error::without_url)?
            .error_for_status()
            .map_err(reqwest::Error::without_url)?;
        Ok(())
    }
}
fn escape(s: &str) -> String {
    url::form_urlencoded::byte_serialize(s.as_bytes())
        .collect::<String>()
        .replace('+', "%20")
        .replace('*', "%2A")
        .replace("%7E", "~")
}
