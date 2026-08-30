package wiki.chiu.micro.search.application.model;

import java.util.List;

public record SearchPage<T>(
    List<T> content,
    long totalElements,
    int pageNumber,
    int pageSize,
    boolean first,
    boolean last,
    boolean empty,
    int totalPages) {

  public SearchPage {
    content = List.copyOf(content);
  }
}
