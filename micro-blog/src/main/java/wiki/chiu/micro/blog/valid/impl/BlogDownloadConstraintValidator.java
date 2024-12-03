package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.valid.BlogSysDownload;

import java.time.LocalDateTime;


public class BlogDownloadConstraintValidator implements ConstraintValidator<BlogSysDownload, BlogDownloadReq> {

    @Override
    public boolean isValid(BlogDownloadReq query, ConstraintValidatorContext context) {


        String keywords = query.keywords();
        if (StringUtils.hasLength(keywords) && keywords.length() > 20) {
            return false;
        }

        LocalDateTime end = query.createEnd();
        LocalDateTime start = query.createStart();
        return !start.isAfter(end) && !start.isEqual(end);
    }
}
