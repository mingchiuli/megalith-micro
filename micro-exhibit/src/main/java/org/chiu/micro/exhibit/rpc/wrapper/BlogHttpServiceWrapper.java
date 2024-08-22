package org.chiu.micro.exhibit.rpc.wrapper;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.dto.BlogSensitiveContentDto;
import org.chiu.micro.exhibit.exception.MissException;
import org.chiu.micro.exhibit.lang.Result;
import org.chiu.micro.exhibit.page.PageAdapter;
import org.chiu.micro.exhibit.rpc.BlogHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * BlogHttpServiceWrapper
 */
@Component
@RequiredArgsConstructor
public class BlogHttpServiceWrapper {

    private final BlogHttpService blogHttpService;

    public BlogEntityDto findById(Long blogId, Integer year) {
        Result<BlogEntityDto> result = blogHttpService.findById(blogId);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }

        BlogEntityDto data = result.getData();
        if (Objects.isNull(data)) {
            return BlogEntityDto.builder()
                    .id(blogId)
                    .created(LocalDateTime.of(year, 1, 1, 1, 1, 1))
                    .build();
        }
        return data;
    }

    public BlogEntityDto findById(Long blogId) {
        Result<BlogEntityDto> result = blogHttpService.findById(blogId);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public List<BlogEntityDto> findAllById(List<Long> ids) {
        Result<List<BlogEntityDto>> result = blogHttpService.findAllById(ids);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public List<Integer> getYears() {
        Result<List<Integer>> result = blogHttpService.getYears();
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public Long count() {
        Result<Long> result = blogHttpService.count();
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public List<Long> findIds(Integer pageNo, Integer pageSize) {
        Result<List<Long>> result = blogHttpService.findIds(pageNo, pageSize);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public void setReadCount(Long id) {
        blogHttpService.setReadCount(id);
    }

    public Integer findStatusById(Long blogId) {
        Result<Integer> result = blogHttpService.findStatusById(blogId);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public PageAdapter<BlogEntityDto> findPage(Integer pageNo, Integer pageSize) {
        Result<PageAdapter<BlogEntityDto>> result = blogHttpService.findPage(pageNo, pageSize);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public PageAdapter<BlogEntityDto> findPageByCreatedBetween(Integer pageNo, Integer pageSize, LocalDateTime start, LocalDateTime end) {
        Result<PageAdapter<BlogEntityDto>> result = blogHttpService.findPageByCreatedBetween(pageNo, pageSize, start, end);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public Long countByCreatedBetween(LocalDateTime start, LocalDateTime end) {
        Result<Long> result = blogHttpService.countByCreatedBetween(start, end);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public long countByCreatedGreaterThanEqual(LocalDateTime created) {
        Result<Long> result = blogHttpService.countByCreatedGreaterThanEqual(created);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public long getPageCountYear(LocalDateTime created, LocalDateTime start, LocalDateTime end) {
        Result<Long> result = blogHttpService.getPageCountYear(created, start, end);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

    public BlogSensitiveContentDto findSensitiveByBlogId(Long blogId) {
        Result<BlogSensitiveContentDto> result = blogHttpService.findSensitiveByBlogId(blogId);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }

}