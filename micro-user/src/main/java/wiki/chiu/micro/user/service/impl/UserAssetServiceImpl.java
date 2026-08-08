package wiki.chiu.micro.user.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.rpc.storage.ImageUploadValidator;
import wiki.chiu.micro.common.rpc.storage.ImageUploadValidator.ValidatedImage;
import wiki.chiu.micro.common.rpc.storage.ObjectStorageClient;
import wiki.chiu.micro.user.service.RegistrationTokenStore;
import wiki.chiu.micro.user.service.UserAssetService;
import wiki.chiu.micro.user.service.UserUpload;

@Service
public class UserAssetServiceImpl implements UserAssetService {

  private static final String AVATAR_PREFIX = "avatar/";

  private final RegistrationTokenStore tokens;
  private final ObjectStorageClient storage;

  public UserAssetServiceImpl(RegistrationTokenStore tokens, ObjectStorageClient storage) {
    this.tokens = tokens;
    this.storage = storage;
  }

  @Override
  public String upload(String registrationToken, UserUpload upload) {
    tokens.requireValid(registrationToken);
    ValidatedImage image = ImageUploadValidator.validate(upload.content());
    String objectName =
        ownerPrefix(registrationToken) + UUID.randomUUID() + "." + image.extension();
    return storage.put(objectName, image.content(), image.contentType());
  }

  @Override
  public void delete(String registrationToken, String url) {
    tokens.requireValid(registrationToken);
    String objectName = storage.objectName(url);
    if (!objectName.startsWith(ownerPrefix(registrationToken))) {
      throw new ValidationException("registration avatar belongs to another token");
    }
    storage.delete(objectName);
  }

  private static String ownerPrefix(String token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return AVATAR_PREFIX + HexFormat.of().formatHex(digest) + "/";
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
