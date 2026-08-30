package wiki.chiu.micro.user.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.user.application.model.UserUpload;
import wiki.chiu.micro.user.application.port.in.UserAssetService;
import wiki.chiu.micro.user.application.port.out.RegistrationTokenStore;
import wiki.chiu.micro.user.application.port.out.UserAssetStorage;

@Service
public class UserAssetServiceImpl implements UserAssetService {

  private static final String AVATAR_PREFIX = "avatar/";

  private final RegistrationTokenStore tokens;
  private final UserAssetStorage storage;

  public UserAssetServiceImpl(RegistrationTokenStore tokens, UserAssetStorage storage) {
    this.tokens = tokens;
    this.storage = storage;
  }

  @Override
  public String upload(String registrationToken, UserUpload upload) {
    tokens.requireValid(registrationToken);
    String objectName = ownerPrefix(registrationToken) + UUID.randomUUID();
    return storage.storeImage(objectName, upload.content());
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
