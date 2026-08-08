package wiki.chiu.micro.blog.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_AUTH;

import java.util.UUID;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.blog.service.BlogAssetService;
import wiki.chiu.micro.blog.service.UploadObject;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.rpc.storage.ImageUploadValidator;
import wiki.chiu.micro.common.rpc.storage.ImageUploadValidator.ValidatedImage;
import wiki.chiu.micro.common.rpc.storage.ObjectStorageClient;

@Service
public class BlogAssetServiceImpl implements BlogAssetService {

  private final ObjectStorageClient storage;

  public BlogAssetServiceImpl(ObjectStorageClient storage) {
    this.storage = storage;
  }

  @Override
  public String upload(UploadObject upload, Long userId) {
    ValidatedImage image = ImageUploadValidator.validate(upload.content());
    String objectName = ownerPrefix(userId) + UUID.randomUUID() + "." + image.extension();
    return storage.put(objectName, image.content(), image.contentType());
  }

  @Override
  public void delete(String url, Long userId) {
    String objectName = storage.objectName(url);
    if (!objectName.startsWith(ownerPrefix(userId))) {
      throw new MissException(NO_AUTH);
    }
    storage.delete(objectName);
  }

  private static String ownerPrefix(Long userId) {
    return "blog/" + userId + "/";
  }
}
