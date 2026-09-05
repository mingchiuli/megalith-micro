package wiki.chiu.micro.search.api.vo;

public record IndexRebuildRpcVo(String previousIndex, String index, long documents) {
}
