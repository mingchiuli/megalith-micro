package wiki.chiu.micro.blog.application.model;

import java.util.List;

/**
 * The blog a client submits, before it is checked against the stored state.
 */
public record BlogDraft(
    Long id,
    Long draftRevision,
    String title,
    String description,
    String content,
    Integer status,
    String link,
    List<SensitiveContentDraft> sensitiveContentList) {

    public BlogDraft(
        Long id,
        String title,
        String description,
        String content,
        Integer status,
        String link,
        List<SensitiveContentDraft> sensitiveContentList) {
        this(id, null, title, description, content, status, link, sensitiveContentList);
    }

}
