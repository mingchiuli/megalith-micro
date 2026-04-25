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

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn deserialize_and_access_fields() {
        let json = r#"{"code":200,"msg":"ok","data":42}"#;
        let r: ApiResult<i32> = serde_json::from_str(json).unwrap();
        assert_eq!(r.code(), 200);
        assert_eq!(r.into_data(), 42);
    }

    #[test]
    fn deserialize_with_error_code() {
        let json = r#"{"code":401,"msg":"unauthorized","data":""}"#;
        let r: ApiResult<String> = serde_json::from_str(json).unwrap();
        assert_eq!(r.code(), 401);
        assert_eq!(r.into_data(), "");
    }
}
