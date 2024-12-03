package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.SensitiveContentReq;
import wiki.chiu.micro.blog.valid.BlogSaveValue;
import wiki.chiu.micro.common.lang.SensitiveTypeEnum;
import wiki.chiu.micro.common.lang.StatusEnum;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.stream.Collectors;



public class BlogSaveConstraintValidator implements ConstraintValidator<BlogSaveValue, BlogEntityReq> {

    private static final Pattern pattern = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

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

        Set<Integer> statusSet = Arrays.stream(StatusEnum.values())
                .map(StatusEnum::getCode)
                .collect(Collectors.toSet());
        
        if (!statusSet.contains(status)) {
            return false;
        }

        String link = blog.link();

        if (Objects.isNull(link)) {
            return false;
        }

        if (StringUtils.hasLength(link) && !pattern.matcher(link).matches()) {
            return false;
        }

        List<SensitiveContentReq> sensitiveContentList = blog.sensitiveContentList();

        Set<Integer> sensitiveSet = Arrays.stream(SensitiveTypeEnum.values())
                .map(SensitiveTypeEnum::getCode)
                .collect(Collectors.toSet());
        for (var sensitive : sensitiveContentList) {
            if (Objects.isNull(sensitive.startIndex())) {
                return false;
            }

            if (Objects.isNull(sensitive.endIndex())) {
                return false;
            }

            if (!sensitiveSet.contains(sensitive.type())) {
                return false;
            }
        }

        return true;
    }
}
