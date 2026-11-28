package wiki.chiu.micro.blog.application.model;

public record UploadObject(byte[] content) {

    public UploadObject {
        content = content == null ? new byte[0] : content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
