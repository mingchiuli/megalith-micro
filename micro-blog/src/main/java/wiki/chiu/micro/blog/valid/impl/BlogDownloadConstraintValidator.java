package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.valid.BlogSysDownload;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

import java.time.LocalDateTime;




public class BlogDownloadConstraintValidator implements ConstraintValidator<BlogSysDownload, BlogDownloadReq> {


    @Override
    public boolean isValid(BlogDownloadReq query, ConstraintValidatorContext context) {
        return isValidKeywords(query.keywords()) &&
                isValidStatus(query.status()) &&
                isValidDateRange(query.createStart(), query.createEnd());
    }

    private boolean isValidKeywords(String keywords) {
        return !StringUtils.hasLength(keywords) || keywords.length() <= 20;
    }

    private boolean isValidStatus(Integer status) {
        return status == null || BlogStatusEnum.BLOG_STATUS_SET.contains(status);
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
