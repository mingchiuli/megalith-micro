package wiki.chiu.micro.search.application.model;

import wiki.chiu.micro.search.domain.BlogIndexEntry;

public record BlogIndexChange(Operation operation, long revision, BlogIndexEntry blog) {

  public enum Operation {
    CREATE,
    UPDATE,
    REMOVE
  }
}
