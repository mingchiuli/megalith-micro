package wiki.chiu.micro.exhibit.domain;

import java.util.List;

import wiki.chiu.micro.common.enums.SensitiveTypeEnum;

/**
 * Masks the parts of a blog field that were flagged as sensitive.
 */
public final class SensitiveContentMasker {

    private static final String MASK = "+";

    private SensitiveContentMasker() {
    }

    /**
     * Replaces every sensitive span of the requested field with mask characters.
     *
     * @param content the original field value
     * @param spans every sensitive span of the blog
     * @param type the field to mask
     * @return the masked field value
     */
    public static String mask(String content, List<SensitiveSpan> spans, SensitiveTypeEnum type) {
        String masked = content;
        for (SensitiveSpan span : spans) {
            if (span.type() != type) {
                continue;
            }
            masked =
                masked.substring(0, span.startIndex())
                    + MASK.repeat(span.endIndex() - span.startIndex())
                    + masked.substring(span.endIndex());
        }
        return masked;
    }
}
