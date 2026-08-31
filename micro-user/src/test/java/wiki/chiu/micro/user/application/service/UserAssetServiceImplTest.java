package wiki.chiu.micro.user.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.user.application.model.UserUpload;
import wiki.chiu.micro.user.application.port.out.RegistrationTokenStore;
import wiki.chiu.micro.user.application.port.out.UserAssetStorage;

class UserAssetServiceImplTest {

    private final RegistrationTokenStore tokens = mock(RegistrationTokenStore.class);
    private final UserAssetStorage storage = mock(UserAssetStorage.class);
    private final UserAssetServiceImpl service = new UserAssetServiceImpl(tokens, storage);

    @Test
    void storesImageUnderTokenOwnerHash() {
        when(storage.storeImage(anyString(), any())).thenReturn("https://cdn/avatar.png");

        service.upload("token-a", new UserUpload(png()));

        ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
        verify(storage).storeImage(objectName.capture(), any());
        assertTrue(objectName.getValue().matches("avatar/[0-9a-f]{64}/[0-9a-f-]+"));
    }

    @Test
    void rejectsDeletionOwnedByAnotherRegistrationToken() {
        when(storage.storeImage(anyString(), any())).thenReturn("https://cdn/avatar.png");
        service.upload("token-a", new UserUpload(png()));
        ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
        verify(storage).storeImage(objectName.capture(), any());
        when(storage.objectName("https://cdn/avatar.png"))
            .thenReturn(objectName.getValue() + ".png");

        assertThrows(
            ValidationException.class, () -> service.delete("token-b", "https://cdn/avatar.png"));

        verify(storage, never()).delete(anyString());
    }

    private static byte[] png() {
        return new byte[]{1, 2, 3};
    }
}
