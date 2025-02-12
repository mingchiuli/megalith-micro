use serde::Deserialize;

// Custom error type for auth-related errors
#[derive(Deserialize, Debug)]
pub struct ApiResult<T> {
    code: i32,
    msg: String,
    data: T,
}

impl<T> ApiResult<T> {
    /// Creates a new response
    pub fn new(code: i32, msg: String, data: T) -> Self {
        Self { code, msg, data }
    }

    /// Returns a reference to the response data
    pub fn data(&self) -> &T {
        &self.data
    }

    /// Returns the response code
    pub fn code(&self) -> i32 {
        self.code
    }

    /// Returns a reference to the response message
    pub fn msg(&self) -> &str {
        &self.msg
    }

    /// Consumes the response and returns the data
    pub fn into_data(self) -> T {
        self.data
    }

    /// Consumes the response and returns all fields
    pub fn into_inner(self) -> (i32, String, T) {
        (self.code, self.msg, self.data)
    }
}
