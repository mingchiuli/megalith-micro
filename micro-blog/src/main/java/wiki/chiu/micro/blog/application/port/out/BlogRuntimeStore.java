package wiki.chiu.micro.blog.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import wiki.chiu.micro.blog.application.model.DeletedBlogEntry;
import wiki.chiu.micro.blog.application.model.DeletedBlogPage;
import wiki.chiu.micro.common.lang.BlogSnapshot;

public interface BlogRuntimeStore {

    Map<Long, Integer> readCounts(List<Long> blogIds);

    DeletedBlogPage deletedBlogs(
        Long userId, Integer currentPage, Integer size, LocalDateTime expirationCutoff);

    Optional<DeletedBlogEntry> deletedBlog(Long userId, Integer index);

    void saveDeletedBlog(Long userId, String eventId, BlogSnapshot snapshot);

    void removeDeletedBlog(Long userId, String receipt);

    void saveReadToken(Long blogId, String token);
}
