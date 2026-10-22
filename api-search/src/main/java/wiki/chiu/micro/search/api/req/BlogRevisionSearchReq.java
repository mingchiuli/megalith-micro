package wiki.chiu.micro.search.api.req;

public record BlogRevisionSearchReq(
    Long blogId, Integer page, Integer pageSize, String keywords, Long userId, Boolean allData) {
}
