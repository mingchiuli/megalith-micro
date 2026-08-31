package wiki.chiu.micro.common.rpc.aot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import wiki.chiu.micro.common.lang.Result;

class RpcRuntimeHintsTest {

    @Test
    void registersResultForManualJacksonErrorParsing() {
        RuntimeHints hints = new RuntimeHints();
        new RpcRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(
                RuntimeHintsPredicates.reflection()
                    .onType(Result.class)
                    .withMemberCategory(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                    .and(
                        RuntimeHintsPredicates.reflection()
                            .onType(Result.class)
                            .withMemberCategory(MemberCategory.INVOKE_DECLARED_METHODS))
                    .test(hints))
            .isTrue();
    }
}
