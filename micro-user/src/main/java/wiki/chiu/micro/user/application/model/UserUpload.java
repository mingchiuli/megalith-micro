package wiki.chiu.micro.user.application.model;

public record UserUpload(byte[] content) {

  public UserUpload {
    content = content == null ? new byte[0] : content.clone();
  }

  @Override
  public byte[] content() {
    return content.clone();
  }
}
