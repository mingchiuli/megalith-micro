package wiki.chiu.micro.blog.config;

import wiki.chiu.micro.blog.constant.BlogOperateMessage;
import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {

    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        hints.serialization().registerType(BlogOperateMessage.class);
        hints.serialization().registerType(BlogQueryReq.class);
        hints.serialization().registerType(BlogDownloadReq.class);

        hints.serialization().registerType(BlogDeleteDto.class);

        hints.resources().registerPattern("ValidationMessages.properties");

    }
}
