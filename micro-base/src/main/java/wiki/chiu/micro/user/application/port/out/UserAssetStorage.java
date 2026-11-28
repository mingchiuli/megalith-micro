package wiki.chiu.micro.user.application.port.out;

public interface UserAssetStorage {

    String storeImage(String objectName, byte[] content);

    String objectName(String url);

    void delete(String objectName);
}
