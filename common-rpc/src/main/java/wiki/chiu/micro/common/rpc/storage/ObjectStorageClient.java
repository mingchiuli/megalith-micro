package wiki.chiu.micro.common.rpc.storage;

public interface ObjectStorageClient {

  String put(String objectName, byte[] content, String contentType);

  void delete(String objectName);

  String objectName(String publicUrl);
}
