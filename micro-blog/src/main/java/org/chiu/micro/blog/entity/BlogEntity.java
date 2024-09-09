package org.chiu.micro.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2022-11-27 12:56 am
 */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@Builder
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
@Table(name ="m_blog",
        indexes = {@Index(columnList = "created")})
public class BlogEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "content", length = 65535)
    private String content;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    @Column(name = "status")
    private Integer status;

    @Column(name = "link")
    private String link;

    @Column(name = "read_count")
    private Long readCount;
}
