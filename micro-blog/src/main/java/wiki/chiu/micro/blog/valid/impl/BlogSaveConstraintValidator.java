package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.SensitiveContentReq;
import wiki.chiu.micro.blog.valid.BlogSaveValue;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.SensitiveTypeEnum;
import wiki.chiu.micro.common.lang.BlogStatusEnum;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Collectors;



public class BlogSaveConstraintValidator implements ConstraintValidator<BlogSaveValue, BlogEntityReq> {

    private static final Set<Integer> STATUS_SET = Arrays.stream(BlogStatusEnum.values())
            .map(BlogStatusEnum::getCode)
            .collect(Collectors.toSet());

    private static final Set<Integer> SENSITIVE_SET = Arrays.stream(SensitiveTypeEnum.values())
            .map(SensitiveTypeEnum::getCode)
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(BlogEntityReq blog, ConstraintValidatorContext context) {

        if (!StringUtils.hasLength(blog.title())) {
            return false;
        }

        if (!StringUtils.hasLength(blog.description())) {
            return false;
        }

        if (!StringUtils.hasLength(blog.content())) {
            return false;
        }

        Integer status = blog.status();
        
        if (!STATUS_SET.contains(status)) {
            return false;
        }

        String link = blog.link();

        if (Objects.isNull(link)) {
            return false;
        }

        if (StringUtils.hasLength(link) && !Const.URL_PATTERN.matcher(link).matches()) {
            return false;
        }

        List<SensitiveContentReq> sensitiveContentList = blog.sensitiveContentList();


        for (var sensitive : sensitiveContentList) {
            if (Objects.isNull(sensitive.startIndex())) {
                return false;
            }

            if (Objects.isNull(sensitive.endIndex())) {
                return false;
            }

            if (!SENSITIVE_SET.contains(sensitive.type())) {
                return false;
            }
        }

        return true;
    }
}
