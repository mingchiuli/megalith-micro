package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.valid.BlogSysQuery;
import wiki.chiu.micro.common.lang.Const;

import java.time.LocalDateTime;



public class BlogQueryConstraintValidator implements ConstraintValidator<BlogSysQuery, BlogQueryReq> {

    @Override
    public boolean isValid(BlogQueryReq query, ConstraintValidatorContext context) {
        return isValidSize(query.size()) &&
                isValidCurrentPage(query.currentPage()) &&
                isValidStatus(query.status()) &&
                isValidKeywords(query.keywords()) &&
                isValidDateRange(query.createStart(), query.createEnd());
    }

    private boolean isValidSize(Integer size) {
        return size != null;
    }

    private boolean isValidCurrentPage(Integer currentPage) {
        return currentPage != null;
    }

    private boolean isValidStatus(Integer status) {
        return status == null || Const.BLOG_STATUS_SET.contains(status);
    }

    private boolean isValidKeywords(String keywords) {
        return !StringUtils.hasLength(keywords) || keywords.length() <= 20;
    }

    private boolean isValidDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null && end != null) {
            return false;
        }
        if (start != null && end == null) {
            return false;
        }
        return start == null || (!start.isEqual(end) && !start.isAfter(end));
    }
}
