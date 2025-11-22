package wiki.chiu.micro.blog.config;

import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection

        hints.serialization()
                .registerType(BlogQueryReq.class)
                .registerType(BlogDownloadReq.class)
                .registerType(BlogDeleteDto.class);
        hints.resources().registerPattern("ValidationMessages.properties");

    }
}
