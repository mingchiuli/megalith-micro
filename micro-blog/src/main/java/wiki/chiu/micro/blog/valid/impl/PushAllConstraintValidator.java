package wiki.chiu.micro.blog.valid.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import wiki.chiu.micro.blog.req.BlogEditPushAllReq;
import wiki.chiu.micro.blog.req.SensitiveContentReq;
import wiki.chiu.micro.blog.valid.PushAllValue;
import wiki.chiu.micro.common.lang.Const;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;


public class PushAllConstraintValidator implements ConstraintValidator<PushAllValue, BlogEditPushAllReq> {


    @Override
    public boolean isValid(BlogEditPushAllReq blog, ConstraintValidatorContext context) {
        return isValidTitle(blog.title()) &&
                isValidDescription(blog.description()) &&
                isValidContent(blog.content()) &&
                isValidVersion(blog.version()) &&
                isValidStatus(blog.status()) &&
                isValidLink(blog.link()) &&
                isValidSensitiveContentList(blog.sensitiveContentList());
    }

    private boolean isValidTitle(String title) {
        return Objects.nonNull(title);
    }

    private boolean isValidDescription(String description) {
        return Objects.nonNull(description);
    }

    private boolean isValidContent(String content) {
        return Objects.nonNull(content);
    }

    private boolean isValidVersion(Integer version) {
        return Objects.nonNull(version);
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
