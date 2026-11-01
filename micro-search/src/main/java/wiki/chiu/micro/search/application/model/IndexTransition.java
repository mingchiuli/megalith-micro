package wiki.chiu.micro.search.application.model;

public record IndexTransition(
    SearchIndexKind kind,
    String previousIndex,
    String index,
    long documents) {
}
