package wiki.chiu.micro.blog.adapter.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.domain.BlogEntity;

/**
 * @author mingchiuli
 * @create 2022-11-27 1:30 am
 */
public interface BlogRepository extends JpaRepository<@NonNull BlogEntity, @NonNull Long> {

    @Query(
        "SELECT blog.id FROM BlogEntity blog WHERE blog.id > :afterId ORDER BY blog.id ASC")
    List<Long> findIdsAfter(@Param("afterId") Long afterId, Pageable pageable);

    @Query("""
        SELECT new wiki.chiu.micro.blog.application.model.BlogReadCount(blog.id, COALESCE(blog.readCount, 0L))
        FROM BlogEntity blog WHERE blog.id > :afterId ORDER BY blog.id ASC
        """)
    List<BlogReadCount> findReadCountsAfter(@Param("afterId") long afterId, Pageable pageable);

    @Query("SELECT blog FROM BlogEntity blog WHERE blog.id > :afterId ORDER BY blog.id ASC")
    List<BlogEntity> findSnapshotsAfter(@Param("afterId") long afterId, Pageable pageable);

    @Query(
        value = "UPDATE BlogEntity blog SET blog.readCount = blog.readCount + 1 WHERE blog.id = ?1")
    @Modifying
    void setReadCount(Long id);

    @Modifying
    @Query(
        """
            UPDATE BlogEntity blog
            SET blog.title = :title,
                blog.description = :description,
                blog.content = :content,
                blog.status = :status,
                blog.link = :link,
                blog.updated = :updated,
                blog.eventRevision = :nextRevision
            WHERE blog.id = :id
              AND blog.eventRevision = :expectedRevision
            """)
    int updateByIdAndEventRevision(
        @Param("id") Long id,
        @Param("expectedRevision") Long expectedRevision,
        @Param("nextRevision") Long nextRevision,
        @Param("title") String title,
        @Param("description") String description,
        @Param("content") String content,
        @Param("status") Integer status,
        @Param("link") String link,
        @Param("updated") LocalDateTime updated);

    @Modifying
    @Query(
        "DELETE FROM BlogEntity blog WHERE blog.id = :id AND blog.eventRevision = :expectedRevision")
    int deleteByIdAndEventRevision(
        @Param("id") Long id, @Param("expectedRevision") Long expectedRevision);

    @Query(value = "SELECT blog.userId from BlogEntity blog where blog.id = ?1")
    Long findUserIdById(Long id);

    List<BlogEntity> findByUserIdIn(List<Long> userIds);

    Page<@NonNull BlogEntity> findByStatusIn(PageRequest pageRequest, List<Integer> status);
}
