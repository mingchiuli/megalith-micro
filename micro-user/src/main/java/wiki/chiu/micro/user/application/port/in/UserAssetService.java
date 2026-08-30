package wiki.chiu.micro.user.application.port.in;

import wiki.chiu.micro.user.application.model.UserUpload;

public interface UserAssetService {

  String upload(String registrationToken, UserUpload upload);

  void delete(String registrationToken, String url);
}
