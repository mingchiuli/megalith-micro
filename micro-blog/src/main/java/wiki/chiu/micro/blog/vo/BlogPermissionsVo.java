package wiki.chiu.micro.blog.vo;

public record BlogPermissionsVo(
    boolean collaborate, boolean commit, boolean manageMetadata, boolean manageAssets) {
}
