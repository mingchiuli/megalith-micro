use anyhow::{Context, Result};
use megalith_api::blog::BlogSensitiveSpan;

pub fn mask(text: &str, spans: &[BlogSensitiveSpan], field: i32) -> Result<String> {
    let mut units: Vec<u16> = text.encode_utf16().collect();
    for span in spans.iter().filter(|s| s.r#type == Some(field)) {
        let start = usize::try_from(span.start_index.context("missing span start")?)?;
        let end = usize::try_from(span.end_index.context("missing span end")?)?;
        units
            .get_mut(start..end)
            .context("sensitive span outside text")?
            .fill(b'+' as u16);
    }
    Ok(String::from_utf16(&units)?)
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn masks_java_utf16_offsets() {
        let span = BlogSensitiveSpan {
            start_index: Some(1),
            end_index: Some(4),
            r#type: Some(3),
        };
        assert_eq!(mask("甲😀乙z", &[span], 3).unwrap(), "甲+++z");
    }
}
