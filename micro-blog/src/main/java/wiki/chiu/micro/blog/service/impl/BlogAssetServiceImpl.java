package wiki.chiu.micro.blog.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH;
import static wiki.chiu.micro.common.lang.ExceptionMessage.UPLOAD_MISS;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.blog.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.blog.service.BlogAssetService;
import wiki.chiu.micro.blog.service.UploadObject;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.rpc.storage.ObjectStorageClient;

@Service
public class BlogAssetServiceImpl implements BlogAssetService {

  private final UserHttpServiceWrapper users;
  private final ObjectStorageClient storage;

  public BlogAssetServiceImpl(UserHttpServiceWrapper users, ObjectStorageClient storage) {
    this.users = users;
    this.storage = storage;
  }

  @Override
  public String upload(UploadObject upload, Long userId) {
    byte[] content = upload.content();
    if (content.length == 0) {
      throw new MissException(UPLOAD_MISS);
    }
    String owner = safeSegment(users.findById(userId).nickname());
    String filename =
        safeSegment(
            Optional.ofNullable(upload.originalFilename())
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString()));
    String objectName = owner + "/" + UUID.randomUUID() + "-" + filename;
    String contentType =
        Optional.ofNullable(upload.contentType())
            .filter(value -> !value.isBlank())
            .orElse("application/octet-stream");
    return storage.put(objectName, content, contentType);
  }

  @Override
  public void delete(String url, Long userId) {
    String objectName = storage.objectName(url);
    String ownerPrefix = safeSegment(users.findById(userId).nickname()) + "/";
    if (!objectName.startsWith(ownerPrefix)) {
      throw new MissException(NO_AUTH);
    }
    storage.delete(objectName);
  }

  private static String safeSegment(String value) {
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
