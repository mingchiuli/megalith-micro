package wiki.chiu.micro.blog.application.port.out;

public interface BlogAssetStorage {

    String storeImage(String objectName, byte[] content);

    String objectName(String url);

    void delete(String objectName);
}
