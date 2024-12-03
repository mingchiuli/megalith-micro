package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.valid.BlogSysQuery;

import java.time.LocalDateTime;


public class BlogQueryConstraintValidator implements ConstraintValidator<BlogSysQuery, BlogQueryReq> {

    @Override
    public boolean isValid(BlogQueryReq query, ConstraintValidatorContext context) {

        if (query.size() == null) {
            return false;
        }

        if (query.currentPage() == null) {
            return false;
        }

        String keywords = query.keywords();
        if (StringUtils.hasLength(keywords) && keywords.length() > 20) {
            return false;
        }

        LocalDateTime end = query.createEnd();
        LocalDateTime start = query.createStart();

        if (start == null && end != null) {
            return false;
        }

        if (start != null && end == null) {
            return false;
        }

        return start == null || (!start.isEqual(end) && !start.isAfter(end));
    }
}
