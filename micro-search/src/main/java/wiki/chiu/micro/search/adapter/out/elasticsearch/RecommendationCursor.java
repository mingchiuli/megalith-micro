package wiki.chiu.micro.search.adapter.out.elasticsearch;

record RecommendationCursor(
    int version,
    String pitId,
    long snapshotEpochMillis,
    double score,
    long id) {
}
