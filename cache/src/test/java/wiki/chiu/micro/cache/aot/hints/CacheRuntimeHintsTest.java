package wiki.chiu.micro.cache.aot.hints;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

class CacheRuntimeHintsTest {

  @Test
  void registersCaffeineImplementationsSelectedByLocalCacheConfiguration() {
    RuntimeHints hints = new RuntimeHints();
    new CacheRuntimeHints().registerHints(hints, getClass().getClassLoader());

    assertThat(hasDeclaredConstructors("com.github.benmanes.caffeine.cache.PSAMS", hints)).isTrue();
    assertThat(hasDeclaredConstructors("com.github.benmanes.caffeine.cache.SSMSA", hints)).isTrue();
  }

  private boolean hasDeclaredConstructors(String typeName, RuntimeHints hints) {
    return RuntimeHintsPredicates.reflection()
        .onType(TypeReference.of(typeName))
        .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
        .test(hints);
  }
}
