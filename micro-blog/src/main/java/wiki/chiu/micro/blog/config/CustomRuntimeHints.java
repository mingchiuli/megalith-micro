package wiki.chiu.micro.blog.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.valid.impl.BlogDownloadConstraintValidator;
import wiki.chiu.micro.blog.valid.impl.BlogQueryConstraintValidator;
import wiki.chiu.micro.blog.valid.impl.BlogSaveConstraintValidator;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(BlogDeleteDto.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(BlogQueryReq.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(BlogDownloadReq.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(BlogSaveConstraintValidator.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(BlogQueryConstraintValidator.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(BlogDownloadConstraintValidator.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // ValidationMessages.properties for Bean Validation
        hints.resources()
                .registerPattern("ValidationMessages.properties");
    }
}
