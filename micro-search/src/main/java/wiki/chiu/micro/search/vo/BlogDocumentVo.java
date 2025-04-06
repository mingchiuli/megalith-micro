package wiki.chiu.micro.search.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author mingchiuli
 * @create 2021-12-12 6:55 AM
 */
public record BlogDocumentVo(

        Long id,

        Long userId,

        Integer status,

        String title,

        String description,

        String content,

        String link,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        Float score,

        Map<String, List<String>> highlight) {


    public static BlogDocumentVoBuilder builder() {
        return new BlogDocumentVoBuilder(null, null, null, null, null, null, null, null, null, Collections.emptyMap());
    }

    public record BlogDocumentVoBuilder(
            Long id,
            Long userId,
            Integer status,
            String title,
            String description,
            String content,
            String link,
            LocalDateTime created,
            Float score,
            Map<String, List<String>> highlight
    ) {
        public BlogDocumentVoBuilder id(Long id) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder userId(Long userId) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder status(Integer status) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder title(String title) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder description(String description) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder content(String content) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder link(String link) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder created(LocalDateTime created) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder score(Float score) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVoBuilder highlight(Map<String, List<String>> highlight) {
            return new BlogDocumentVoBuilder(id, userId, status, title, description, content, link, created, score, highlight);
        }

        public BlogDocumentVo build() {
            return new BlogDocumentVo(id, userId, status, title, description, content, link, created, score, highlight);
        }
    }
}
