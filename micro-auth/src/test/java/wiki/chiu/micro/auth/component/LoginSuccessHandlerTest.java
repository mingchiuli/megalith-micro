package wiki.chiu.micro.auth.component;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.auth.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.auth.token.JwtTokenService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LoginSuccessHandlerTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserHttpServiceWrapper userHttpServiceWrapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private Authentication authentication;

    @Mock
    private FilterChain filterChain;

    @Test
    void doesNotContinueFilterChainAfterWritingResponse() throws Exception {
        LoginSuccessHandler handler = new LoginSuccessHandler(
                JsonMapper.builder().build(), jwtTokenService, userHttpServiceWrapper, redissonClient);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest("POST", "/login"),
                response,
                filterChain,
                authentication);

        assertTrue(response.getContentAsString().contains("用户不存在"));
        verifyNoInteractions(filterChain);
    }
}
