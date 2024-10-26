package wiki.chiu.micro.exhibit.rpc;


import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.dto.BlogSensitiveContentRpcDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * BlogHttpServiceWrapper
 */
@Component
public class BlogHttpServiceWrapper {

    private final BlogHttpService blogHttpService;

    public BlogHttpServiceWrapper(BlogHttpService blogHttpService) {
        this.blogHttpService = blogHttpService;
    }

    public BlogEntityRpcDto findById(Long blogId, Integer year) {
        Result<BlogEntityRpcDto> result = blogHttpService.findById(blogId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }

        BlogEntityRpcDto data = result.data();
        if (Objects.isNull(data)) {
            return BlogEntityRpcDto.builder()
                    .id(blogId)
                    .created(LocalDateTime.of(year, 1, 1, 1, 1, 1))
                    .build();
        }
        return data;
    }

    public BlogEntityRpcDto findById(Long blogId) {
        Result<BlogEntityRpcDto> result = blogHttpService.findById(blogId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public List<BlogEntityRpcDto> findAllById(List<Long> ids) {
        Result<List<BlogEntityRpcDto>> result = blogHttpService.findAllById(ids);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public List<Integer> getYears() {
        Result<List<Integer>> result = blogHttpService.getYears();
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public Long count() {
        Result<Long> result = blogHttpService.count();
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public List<Long> findIds(Integer pageNo, Integer pageSize) {
        Result<List<Long>> result = blogHttpService.findIds(pageNo, pageSize);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public void setReadCount(Long id) {
        blogHttpService.setReadCount(id);
    }

    public Integer findStatusById(Long blogId) {
        Result<Integer> result = blogHttpService.findStatusById(blogId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public PageAdapter<BlogEntityRpcDto> findPage(Integer pageNo, Integer pageSize) {
        Result<PageAdapter<BlogEntityRpcDto>> result = blogHttpService.findPage(pageNo, pageSize);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public PageAdapter<BlogEntityRpcDto> findPageByCreatedBetween(Integer pageNo, Integer pageSize, LocalDateTime start, LocalDateTime end) {
        Result<PageAdapter<BlogEntityRpcDto>> result = blogHttpService.findPageByCreatedBetween(pageNo, pageSize, start, end);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public Long countByCreatedBetween(LocalDateTime start, LocalDateTime end) {
        Result<Long> result = blogHttpService.countByCreatedBetween(start, end);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public long countByCreatedGreaterThanEqual(LocalDateTime created) {
        Result<Long> result = blogHttpService.countByCreatedGreaterThanEqual(created);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public long getPageCountYear(LocalDateTime created, LocalDateTime start, LocalDateTime end) {
        Result<Long> result = blogHttpService.getPageCountYear(created, start, end);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public BlogSensitiveContentRpcDto findSensitiveByBlogId(Long blogId) {
        Result<BlogSensitiveContentRpcDto> result = blogHttpService.findSensitiveByBlogId(blogId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}