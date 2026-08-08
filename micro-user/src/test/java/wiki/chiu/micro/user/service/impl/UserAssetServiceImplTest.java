package wiki.chiu.micro.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.rpc.storage.ObjectStorageClient;
import wiki.chiu.micro.user.service.RegistrationTokenStore;
import wiki.chiu.micro.user.service.UserUpload;

class UserAssetServiceImplTest {

  private final RegistrationTokenStore tokens = mock(RegistrationTokenStore.class);
  private final ObjectStorageClient storage = mock(ObjectStorageClient.class);
  private final UserAssetServiceImpl service = new UserAssetServiceImpl(tokens, storage);

  @Test
  void storesValidatedImageUnderTokenOwnerHash() throws IOException {
    when(storage.put(anyString(), any(), eq("image/png"))).thenReturn("https://cdn/avatar.png");

    service.upload("token-a", new UserUpload(png()));

    ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
    verify(storage).put(objectName.capture(), any(), eq("image/png"));
    assertTrue(objectName.getValue().matches("avatar/[0-9a-f]{64}/[0-9a-f-]+\\.png"));
  }

  @Test
  void rejectsDeletionOwnedByAnotherRegistrationToken() throws IOException {
    when(storage.put(anyString(), any(), eq("image/png"))).thenReturn("https://cdn/avatar.png");
    service.upload("token-a", new UserUpload(png()));
    ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
    verify(storage).put(objectName.capture(), any(), eq("image/png"));
    when(storage.objectName("https://cdn/avatar.png")).thenReturn(objectName.getValue());

    assertThrows(
        ValidationException.class, () -> service.delete("token-b", "https://cdn/avatar.png"));

    verify(storage, never()).delete(anyString());
  }

  private static byte[] png() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
    return output.toByteArray();
  }
}
