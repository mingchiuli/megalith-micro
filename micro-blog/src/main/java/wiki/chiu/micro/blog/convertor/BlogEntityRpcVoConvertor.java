package wiki.chiu.micro.blog.convertor;

import java.util.List;

import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;
import org.springframework.data.domain.Page;

public class BlogEntityRpcVoConvertor {

    private BlogEntityRpcVoConvertor() {
    }

    public static BlogEntityRpcVo convert(BlogEntity blogEntity) {
        return BlogEntityRpcVo.builder()
                .id(blogEntity.getId())
                .title(blogEntity.getTitle())
                .link(blogEntity.getLink())
                .readCount(blogEntity.getReadCount())
                .description(blogEntity.getDescription())
                .content(blogEntity.getContent())
                .userId(blogEntity.getUserId())
                .created(blogEntity.getCreated())
                .updated(blogEntity.getUpdated())
                .status(blogEntity.getStatus())
                .build();
    }

    public static List<BlogEntityRpcVo> convert(List<BlogEntity> blogEntities) {
        return blogEntities.stream()
                .map(item -> BlogEntityRpcVo.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .link(item.getLink())
                        .readCount(item.getReadCount())
                        .description(item.getDescription())
                        .content(item.getContent())
                        .userId(item.getUserId())
                        .created(item.getCreated())
                        .updated(item.getUpdated())
                        .status(item.getStatus())
                        .build())
                .toList();
    }

    public static PageAdapter<BlogEntityRpcVo> convert(Page<BlogEntity> page) {
        List<BlogEntityRpcVo> out = page.getContent().stream()
                .map(item -> BlogEntityRpcVo.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .link(item.getLink())
                        .readCount(item.getReadCount())
                        .description(item.getDescription())
                        .content(item.getContent())
                        .userId(item.getUserId())
                        .created(item.getCreated())
                        .updated(item.getUpdated())
                        .status(item.getStatus())
                        .build())
                .toList();

        return PageAdapter.<BlogEntityRpcVo>builder()
                .content(out)
                .empty(page.isEmpty())
                .totalElements(page.getTotalElements())
                .pageNumber(page.getPageable().getPageNumber() + 1)
                .pageSize(page.getPageable().getPageSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .totalPages(page.getTotalPages())
                .build();
    }
}
