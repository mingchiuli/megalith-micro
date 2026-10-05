package wiki.chiu.micro.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.application.model.CodeVerification;
import wiki.chiu.micro.auth.application.model.LoginChannel;
import wiki.chiu.micro.auth.application.port.out.LoginCodeStore;

class LoginCodeAuthenticationServiceImplTest {

    private final LoginCodeStore codes = mock(LoginCodeStore.class);
    private final LoginCodeAuthenticationServiceImpl service =
        new LoginCodeAuthenticationServiceImpl(codes, 3);

    @Test
    void mapsAnAcceptedEmailCode() {
        when(codes.verify(LoginCodeStore.LoginChannel.EMAIL, "a@b.c", "123456", 3))
            .thenReturn(LoginCodeStore.Verification.ACCEPTED);

        assertThat(service.authenticate(LoginChannel.EMAIL, "a@b.c", "123456"))
            .isEqualTo(CodeVerification.ACCEPTED);
    }

    @Test
    void mapsAnExhaustedSmsCode() {
        when(codes.verify(LoginCodeStore.LoginChannel.PHONE, "138", "000000", 3))
            .thenReturn(LoginCodeStore.Verification.ATTEMPTS_EXCEEDED);

        assertThat(service.authenticate(LoginChannel.PHONE, "138", "000000"))
            .isEqualTo(CodeVerification.ATTEMPTS_EXCEEDED);
    }
}
