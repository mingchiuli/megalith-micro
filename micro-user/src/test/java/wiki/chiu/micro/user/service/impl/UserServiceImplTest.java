package wiki.chiu.micro.user.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.wrapper.UserRoleWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Test
    void registrationRejectsExpiredTokenBeforePersistence() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        UserRoleWrapper wrapper = mock(UserRoleWrapper.class);
        UserServiceImpl service = new UserServiceImpl(
                mock(UserRepository.class),
                redisTemplate,
                mock(OssHttpService.class),
                mock(TaskExecutor.class),
                wrapper,
                mock(PasswordEncoder.class),
                mock(RoleRepository.class),
                mock(UserRoleRepository.class));
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        UserEntityRegisterReq request = new UserEntityRegisterReq(
                "alice", "Alice", "https://example.com/avatar.jpg", "secret", "secret",
                "alice@example.com", "13800138000", "expired-token");

        MissException exception = assertThrows(MissException.class,
                () -> service.saveRegisterPage(request));

        assertEquals("没有权限", exception.getMessage());
        verify(wrapper, never()).saveOrUpdate(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyList());
    }
}
