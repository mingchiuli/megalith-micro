package wiki.chiu.micro.user.service;

public interface UserAssetService {

  String upload(String registrationToken, UserUpload upload);

  void delete(String registrationToken, String url);
}
