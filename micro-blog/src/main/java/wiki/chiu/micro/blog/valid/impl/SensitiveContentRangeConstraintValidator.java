package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.SensitiveContentReq;
import wiki.chiu.micro.blog.valid.ValidSensitiveContentRange;

public class SensitiveContentRangeConstraintValidator
        implements ConstraintValidator<ValidSensitiveContentRange, BlogEntityReq> {

    @Override
    public boolean isValid(BlogEntityReq request, ConstraintValidatorContext context) {
        if (request == null || request.content() == null || request.sensitiveContentList() == null) {
            return true;
        }
        int contentLength = request.content().length();
        return request.sensitiveContentList().stream()
                .allMatch(item -> isValidRange(item, contentLength));
    }

    private static boolean isValidRange(SensitiveContentReq item, int contentLength) {
        if (item == null || item.startIndex() == null || item.endIndex() == null) {
            return true;
        }
        return item.startIndex() >= 0
                && item.startIndex() < item.endIndex()
                && item.endIndex() <= contentLength;
    }
}
