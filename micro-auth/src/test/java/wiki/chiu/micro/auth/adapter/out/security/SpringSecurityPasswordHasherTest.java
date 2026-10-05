package wiki.chiu.micro.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class SpringSecurityPasswordHasherTest {

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final SpringSecurityPasswordHasher hasher =
        new SpringSecurityPasswordHasher(passwordEncoder);

    @Test
    void delegatesTheComparisonToThePasswordEncoder() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        assertThat(hasher.matches("raw", "encoded")).isTrue();
    }
}
