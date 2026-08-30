package wiki.chiu.micro.blog.adapter.out.storage;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.blog.application.port.out.BlogAssetStorage;
import wiki.chiu.micro.common.rpc.storage.ImageUploadValidator;
import wiki.chiu.micro.common.rpc.storage.ObjectStorageClient;

@Component
public class ObjectStorageBlogAssetAdapter implements BlogAssetStorage {

  private final ObjectStorageClient storage;

  public ObjectStorageBlogAssetAdapter(ObjectStorageClient storage) {
    this.storage = storage;
  }

  @Override
  public String storeImage(String objectName, byte[] content) {
    var image = ImageUploadValidator.validate(content);
    return storage.put(
        objectName + "." + image.extension(), image.content(), image.contentType());
  }

  @Override
  public String objectName(String url) {
    return storage.objectName(url);
  }

  @Override
  public void delete(String objectName) {
    storage.delete(objectName);
  }
}
