package wiki.chiu.micro.blog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2022-12-03 11:36 pm
 */
public record BlogEntityVo(
    Long id,

    String title,

    String description,

    String content,

    String link,

    Long readCount,

    Integer recentReadCount,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updated,

    Integer status
) {
    public static BlogEntityVoBuilder builder() {
        return new BlogEntityVoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record BlogEntityVoBuilder(
        Long id,
        String title,
        String description,
        String content,
        String link,
        Long readCount,
        Integer recentReadCount,
        LocalDateTime created,
        LocalDateTime updated,
        Integer status
    ) {
        public BlogEntityVoBuilder id(Long id) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder title(String title) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder description(String description) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder content(String content) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder link(String link) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder readCount(Long readCount) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder recentReadCount(Integer recentReadCount) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder created(LocalDateTime created) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder updated(LocalDateTime updated) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVoBuilder status(Integer status) {
            return new BlogEntityVoBuilder(id, title, description, content, link, readCount, recentReadCount, created, updated, status);
        }

        public BlogEntityVo build() {
            return new BlogEntityVo(
                id,
                title,
                description,
                content,
                link,
                readCount,
                recentReadCount,
                created,
                updated,
                status
            );
        }
    }
}
