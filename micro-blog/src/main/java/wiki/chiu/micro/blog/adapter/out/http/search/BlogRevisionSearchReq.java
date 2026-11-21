package wiki.chiu.micro.blog.adapter.out.http.search;

public record BlogRevisionSearchReq(
    Long blogId, Integer page, Integer pageSize, String keywords, Long userId, Boolean allData) {
}
