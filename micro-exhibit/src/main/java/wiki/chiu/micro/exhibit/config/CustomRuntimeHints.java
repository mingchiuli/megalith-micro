package wiki.chiu.micro.exhibit.config;

import org.springframework.aot.hint.*;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;

public class CustomRuntimeHints implements RuntimeHintsRegistrar {

  @Override // Register method for reflection
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    // Register method for reflection

    hints
        .reflection()
        .registerType(
            BlogExhibitDto.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS)
        .registerType(
            BlogDescriptionDto.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS)
        .registerType(
            BlogSensitiveContentRpcVo.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS);

    hints
        .resources()
        .registerPattern("script/multi-pfcount.lua")
        .registerPattern("script/compare-delete.lua");
  }
}
