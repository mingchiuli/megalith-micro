package wiki.chiu.micro.cache.aot.hints;

import static org.springframework.util.ReflectionUtils.findMethod;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import wiki.chiu.micro.cache.listener.RabbitCacheEvictMessageListener;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;

class CacheRuntimeHints implements RuntimeHintsRegistrar {

  private static final Logger log = LoggerFactory.getLogger(CacheRuntimeHints.class);

  @SuppressWarnings("all")
  @Override
  public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
    try {
      hints
          .reflection()
          .registerMethod(
              findMethod(
                  RabbitCacheEvictMessageListener.class,
                  "handleMessage",
                  CacheEvictionMessage.class),
              ExecutableMode.INVOKE);

      hints
          .reflection()
          .registerType(
              CacheEvictionMessage.class,
              MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
              MemberCategory.INVOKE_PUBLIC_METHODS);

      hints
          .reflection()
          .registerType(
              TypeReference.of(
                  "wiki.chiu.micro.cache.key.impl.JacksonCacheKeyFactory$KeyArgument"),
              MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
              MemberCategory.INVOKE_DECLARED_METHODS);

      TypeReference ssmsaType = TypeReference.of("com.github.benmanes.caffeine.cache.SSMSA");
      hints
          .reflection()
          .registerType(
              ssmsaType,
              MemberCategory.ACCESS_DECLARED_FIELDS,
              MemberCategory.ACCESS_PUBLIC_FIELDS,
              MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
              MemberCategory.INVOKE_PUBLIC_METHODS)
          .registerType(
              ssmsaType,
              typeHint -> {
                typeHint.withField("FACTORY");
                typeHint.withField("EXPIRES_AFTER_ACCESS_NANOS");
              });

    } catch (Exception e) {
      log.error("Failed to register runtime hints", e);
      throw new IllegalStateException("Application start failed: " + e.getMessage(), e);
    }
  }
}
