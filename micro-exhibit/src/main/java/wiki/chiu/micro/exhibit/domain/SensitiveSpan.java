package wiki.chiu.micro.exhibit.domain;

import wiki.chiu.micro.common.enums.SensitiveTypeEnum;

/**
 * A span of blog content that must be masked before it is shown to a reader.
 *
 * @param type the field the span belongs to
 * @param startIndex inclusive first character offset
 * @param endIndex exclusive last character offset
 */
public record SensitiveSpan(SensitiveTypeEnum type, int startIndex, int endIndex) {

    public SensitiveSpan {
        if (startIndex < 0 || endIndex < startIndex) {
            throw new IllegalArgumentException("Sensitive span must be a non-empty forward range");
        }
    }
}
