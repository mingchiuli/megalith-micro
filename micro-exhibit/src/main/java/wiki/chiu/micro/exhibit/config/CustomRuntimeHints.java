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
                .registerType(BlogExhibitDto.class)
                .registerType(BlogDescriptionDto.class)
                .registerType(BlogSensitiveContentRpcVo.class);

        hints.resources()
                .registerPattern("logback-spring.xml");
    }
}
