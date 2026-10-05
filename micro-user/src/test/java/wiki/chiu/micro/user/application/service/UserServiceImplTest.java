package wiki.chiu.micro.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.user.application.port.out.RegistrationTokenStore;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.application.model.RegistrationDraft;

class UserServiceImplTest {

    @Test
    void registrationRejectsExpiredTokenBeforePersistence() {
        RegistrationTokenStore tokens = mock(RegistrationTokenStore.class);
        UserDraftPersister userDrafts = mock(UserDraftPersister.class);
        RegistrationServiceImpl service =
            new RegistrationServiceImpl(
                tokens, mock(UserReader.class), userDrafts, "https://example.com/register/");
        RegistrationDraft request =
            new RegistrationDraft(
                "alice",
                "Alice",
                "https://example.com/avatar.jpg",
                "secret",
                "alice@example.com",
                "13800138000",
                "expired-token");

        doThrow(new MissException(wiki.chiu.micro.common.error.ExceptionMessage.NO_AUTH))
            .when(tokens)
            .consumeForUsername("expired-token", "alice");

        MissException exception = assertThrows(MissException.class, () -> service.register(request));

        assertEquals("没有权限", exception.getMessage());
        verify(userDrafts, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registrationConsumesTokenBeforePersistingUser() {
        RegistrationTokenStore tokens = mock(RegistrationTokenStore.class);
        UserReader users = mock(UserReader.class);
        UserDraftPersister userDrafts = mock(UserDraftPersister.class);
        RegistrationServiceImpl service =
            new RegistrationServiceImpl(
                tokens, users, userDrafts, "https://example.com/register/");
        when(users.findByUsername("alice")).thenReturn(Optional.empty());
        RegistrationDraft request =
            new RegistrationDraft(
                "alice",
                "Alice",
                "https://example.com/avatar.jpg",
                "secret",
                "alice@example.com",
                "13800138000",
                "token");

        service.register(request);

        InOrder order = inOrder(tokens, userDrafts);
        order.verify(tokens).consumeForUsername("token", "alice");
        order.verify(userDrafts).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registrationPageEncodesUsernameAsOneQueryParameter() {
        RegistrationTokenStore tokens = mock(RegistrationTokenStore.class);
        when(tokens.issue("张 三&#")).thenReturn("token");
        RegistrationServiceImpl service =
            new RegistrationServiceImpl(
                tokens,
                mock(UserReader.class),
                mock(UserDraftPersister.class),
                "https://example.com/register/");

        String page = service.issuePage("张 三&#");

        String rawQuery = URI.create(page).getRawQuery();
        assertFalse(rawQuery.substring("username=".length()).contains("&"));
        assertFalse(rawQuery.contains("#"));
        assertEquals("username=张 三&#", URLDecoder.decode(rawQuery, StandardCharsets.UTF_8));
    }
}
