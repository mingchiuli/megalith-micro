package wiki.chiu.micro.blog.adapter.in.http;

public record BlogPermissionsVo(
    boolean collaborate, boolean commit, boolean manageMetadata, boolean manageAssets) {
}
