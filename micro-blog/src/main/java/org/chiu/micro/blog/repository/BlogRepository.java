package org.chiu.micro.blog.repository;

import org.chiu.micro.blog.entity.BlogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-27 1:30 am
 */
public interface BlogRepository extends JpaRepository<BlogEntity, Long> {

    Long countByCreatedBetween(LocalDateTime start, LocalDateTime end);

    Long countByCreatedGreaterThanEqual(LocalDateTime created);


    @Query(value = "SELECT blog.status from BlogEntity blog where blog.id = ?1")
    Integer findStatusById(Long blogId);

    @Query(value = "SELECT count(blog) from BlogEntity blog where blog.created between :start and :end and blog.created >= :created")
    Long getPageCountYear(@Param("created") LocalDateTime created, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "UPDATE BlogEntity blog SET blog.readCount = blog.readCount + 1 WHERE blog.id = ?1")
    @Modifying
    @Transactional
    void setReadCount(Long id);

    @Query(value = "SELECT DISTINCT(Year(blog.created)) from BlogEntity blog")
    List<Integer> getYears();

    @Query(value = "SELECT blog.id from BlogEntity blog")
    List<Long> findIds(Pageable pageRequest);

    @Query(value = "SELECT blog.userId from BlogEntity blog where blog.id = ?1")
    Long findUserIdById(Long id);

    Page<BlogEntity> findAllByCreatedBetween(PageRequest pageRequest, LocalDateTime start, LocalDateTime end);
}
