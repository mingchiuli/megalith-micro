package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.valid.BlogSysDownload;
import wiki.chiu.micro.common.lang.StatusEnum;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


public class BlogDownloadConstraintValidator implements ConstraintValidator<BlogSysDownload, BlogDownloadReq> {

    private static final Set<Integer> statusSet = Arrays.stream(StatusEnum.values())
            .map(StatusEnum::getCode)
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(BlogDownloadReq query, ConstraintValidatorContext context) {


        String keywords = query.keywords();
        if (StringUtils.hasLength(keywords) && keywords.length() > 20) {
            return false;
        }

        Integer status = query.status();
        if (status != null && !statusSet.contains(status)) {
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
