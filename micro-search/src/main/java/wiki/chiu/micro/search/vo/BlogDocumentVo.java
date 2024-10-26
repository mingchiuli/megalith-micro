package wiki.chiu.micro.search.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
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
        return new BlogDocumentVoBuilder();
    }

    public static class BlogDocumentVoBuilder {
        private Long id;
        private Long userId;
        private Integer status;
        private String title;
        private String description;
        private String content;
        private String link;
        private LocalDateTime created;
        private Float score;
        private Map<String, List<String>> highlight;

        public BlogDocumentVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BlogDocumentVoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogDocumentVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogDocumentVoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BlogDocumentVoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlogDocumentVoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public BlogDocumentVoBuilder link(String link) {
            this.link = link;
            return this;
        }

        public BlogDocumentVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public BlogDocumentVoBuilder score(Float score) {
            this.score = score;
            return this;
        }

        public BlogDocumentVoBuilder highlight(Map<String, List<String>> highlight) {
            this.highlight = highlight;
            return this;
        }

        public BlogDocumentVo build() {
            return new BlogDocumentVo(id, userId, status, title, description, content, link, created, score, highlight);
        }
    }
}
