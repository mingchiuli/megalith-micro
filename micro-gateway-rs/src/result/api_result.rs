use serde::Deserialize;

// Custom error type for auth-related errors
#[derive(Deserialize, Debug)]

pub struct ApiResult<T> {
    code: i32,
    #[allow(dead_code)]
    msg: String,
    data: T,
}

impl<T> ApiResult<T> {

    /// Returns the response code
    pub fn code(&self) -> i32 {
        self.code
    }

    /// Consumes the response and returns the data
    pub fn into_data(self) -> T {
        self.data
    }

}
