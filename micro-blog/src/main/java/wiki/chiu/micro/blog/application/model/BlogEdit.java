package wiki.chiu.micro.blog.application.model;

import java.util.List;

/**
 * The blog as the editor needs it, with the caller's permissions and the marked spans.
 */
public record BlogEdit(
    Long id,
    Long userId,
    String title,
    String description,
    String link,
    String content,
    Integer status,
    List<SensitiveContentDraft> sensitiveContentList,
    BlogPermissions permissions) {
}
