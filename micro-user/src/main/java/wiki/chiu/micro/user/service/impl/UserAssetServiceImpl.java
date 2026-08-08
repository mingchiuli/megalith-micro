package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.UPLOAD_MISS;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.exception.ValidationException;
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
    byte[] content = upload.content();
    if (content.length == 0) {
      throw new MissException(UPLOAD_MISS);
    }
    String filename = safeFilename(upload.originalFilename());
    String objectName = AVATAR_PREFIX + UUID.randomUUID() + "-" + filename;
    String contentType =
        Optional.ofNullable(upload.contentType())
            .filter(value -> !value.isBlank())
            .orElse("application/octet-stream");
    return storage.put(objectName, content, contentType);
  }

  @Override
  public void delete(String registrationToken, String url) {
    tokens.requireValid(registrationToken);
    String objectName = storage.objectName(url);
    if (!objectName.startsWith(AVATAR_PREFIX)) {
      throw new ValidationException("object is not a registration avatar");
    }
    storage.delete(objectName);
  }

  private static String safeFilename(String value) {
    String sanitized =
        value == null
            ? ""
            : value
                .replace("/", "")
                .replace("\\", "")
                .replaceAll("\\p{Cntrl}", "")
                .replace(" ", "");
    return sanitized.isBlank() ? UUID.randomUUID().toString() : sanitized;
  }
}
