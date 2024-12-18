package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.valid.BlogSysDownload;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public class BlogDownloadConstraintValidator implements ConstraintValidator<BlogSysDownload, BlogDownloadReq> {

    private static final Set<Integer> STATUS_SET = Arrays.stream(BlogStatusEnum.values())
            .map(BlogStatusEnum::getCode)
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(BlogDownloadReq query, ConstraintValidatorContext context) {


        String keywords = query.keywords();
        if (StringUtils.hasLength(keywords) && keywords.length() > 20) {
            return false;
        }

        Integer status = query.status();
        if (status != null && !STATUS_SET.contains(status)) {
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
