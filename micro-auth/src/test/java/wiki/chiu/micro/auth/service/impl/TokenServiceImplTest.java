package wiki.chiu.micro.auth.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.token.Claims;
import wiki.chiu.micro.auth.token.TokenUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private TokenUtils<Claims> tokenUtils;

    @Mock
    private UserHttpServiceWrapper userHttpServiceWrapper;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Test
    void refreshTokenRejectsAccessTokenRole() {
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> tokenService.refreshToken(42L, List.of("user"))
        );

        assertEquals("没有权限", exception.getMessage());
        verifyNoInteractions(tokenUtils, userHttpServiceWrapper);
    }
}
