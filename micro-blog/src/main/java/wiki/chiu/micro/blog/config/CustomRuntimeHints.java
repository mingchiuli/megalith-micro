package wiki.chiu.micro.blog.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(BlogQueryReq.class)
                .registerType(BlogDownloadReq.class)
                .registerType(BlogDeleteDto.class);

        // ValidationMessages.properties for Bean Validation
        hints.resources()
                .registerPattern("ValidationMessages.properties");
    }
}
