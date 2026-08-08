package wiki.chiu.micro.blog.service;

public interface BlogAssetService {

  String upload(UploadObject upload, Long userId);

  void delete(String url, Long userId);
}
