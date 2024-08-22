package org.chiu.micro.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name ="m_blog_sensitive_content",
        indexes = {
                @Index(columnList = "blog_id")})
public class BlogSensitiveContentEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blog_id")
    private Long blogId;

    @Column(name = "start_index")
    private Integer startIndex;

    @Column(name = "end_index")
    private Integer endIndex;

    @Column(name = "type")
    private Integer type;

    @Column(name = "created", updatable = false)
    @CreatedDate
    private LocalDateTime created;

    @Column(name = "updated")
    @LastModifiedDate
    private LocalDateTime updated;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlogSensitiveContentEntity that = (BlogSensitiveContentEntity) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(blogId, that.blogId)) return false;
        if (!Objects.equals(created, that.created)) return false;
        if (!Objects.equals(updated, that.updated)) return false;
        return Objects.equals(endIndex, that.endIndex);
    }

    @Override
    public int hashCode() {
      return getClass().hashCode();
    }
}
