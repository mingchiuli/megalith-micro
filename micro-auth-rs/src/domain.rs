pub mod authorization;
pub mod token;

#[derive(Clone, Copy)]
pub enum Channel {
    Email,
    Sms,
}
impl Channel {
    pub(crate) fn key(self, name: &str) -> String {
        format!(
            "{}{name}",
            match self {
                Self::Email => "email_validation:",
                Self::Sms => "phone_validation:",
            }
        )
    }
    pub fn generate(self) -> String {
        let (alphabet, len): (&[u8], usize) = match self {
            Self::Email => (b"abcdefghijklmnopqrstuvwxyz1234567890", 5),
            Self::Sms => (b"1234567890", 6),
        };
        (0..len)
            .map(|_| alphabet[rand::random_range(0..alphabet.len())] as char)
            .collect()
    }
}
