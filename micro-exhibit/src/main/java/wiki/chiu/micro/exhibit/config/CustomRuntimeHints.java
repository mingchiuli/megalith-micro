package wiki.chiu.micro.exhibit.config;

import wiki.chiu.micro.common.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.springframework.aot.hint.*;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection

        hints.reflection()
                .registerType(BlogExhibitDto.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(BlogDescriptionDto.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(BlogSensitiveContentRpcVo.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS);
    }
}
