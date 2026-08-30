package wiki.chiu.micro.user.adapter.out.storage;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.rpc.storage.ImageUploadValidator;
import wiki.chiu.micro.common.rpc.storage.ObjectStorageClient;
import wiki.chiu.micro.user.application.port.out.UserAssetStorage;

@Component
public class ObjectStorageUserAssetAdapter implements UserAssetStorage {

  private final ObjectStorageClient storage;

  public ObjectStorageUserAssetAdapter(ObjectStorageClient storage) {
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
