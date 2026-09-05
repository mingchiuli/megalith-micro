package wiki.chiu.micro.search.application.model;

public record IndexRebuildResult(String previousIndex, String index, long documents) {
}
