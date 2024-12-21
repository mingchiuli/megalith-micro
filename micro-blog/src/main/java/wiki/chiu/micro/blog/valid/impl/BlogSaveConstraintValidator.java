package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.SensitiveContentReq;
import wiki.chiu.micro.blog.valid.BlogSaveValue;
import wiki.chiu.micro.common.lang.Const;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;



public class BlogSaveConstraintValidator implements ConstraintValidator<BlogSaveValue, BlogEntityReq> {



    @Override
    public boolean isValid(BlogEntityReq blog, ConstraintValidatorContext context) {
        return isValidTitle(blog.title()) &&
                isValidDescription(blog.description()) &&
                isValidContent(blog.content()) &&
                isValidStatus(blog.status()) &&
                isValidLink(blog.link()) &&
                isValidSensitiveContentList(blog.sensitiveContentList());
    }

    private boolean isValidTitle(String title) {
        return StringUtils.hasLength(title);
    }

    private boolean isValidDescription(String description) {
        return StringUtils.hasLength(description);
    }

    private boolean isValidContent(String content) {
        return StringUtils.hasLength(content);
    }

    private boolean isValidStatus(Integer status) {
        return Const.BLOG_STATUS_SET.contains(status);
    }

    private boolean isValidLink(String link) {
        return link != null && (!StringUtils.hasLength(link) || Const.URL_PATTERN.matcher(link).matches());
    }

    private boolean isValidSensitiveContentList(List<SensitiveContentReq> sensitiveContentList) {
        for (var sensitive : sensitiveContentList) {
            if (Objects.isNull(sensitive.startIndex()) ||
                    Objects.isNull(sensitive.endIndex()) ||
                    !Const.SENSITIVE_SET.contains(sensitive.type())) {
                return false;
            }
        }
        return true;
    }
}
