package wiki.chiu.micro.user.service;

public record UserUpload(String originalFilename, String contentType, byte[] content) {

  public UserUpload {
    content = content == null ? new byte[0] : content.clone();
  }

  @Override
  public byte[] content() {
    return content.clone();
  }
}
