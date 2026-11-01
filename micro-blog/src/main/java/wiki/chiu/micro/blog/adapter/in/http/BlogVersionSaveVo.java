package wiki.chiu.micro.blog.adapter.in.http;

public record BlogVersionSaveVo(
    Long draftRevision,
    BlogVersionVo version) {
}
