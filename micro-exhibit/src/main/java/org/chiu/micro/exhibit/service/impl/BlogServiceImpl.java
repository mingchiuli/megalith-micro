package org.chiu.micro.exhibit.service.impl;

import org.chiu.micro.exhibit.convertor.BlogDescriptionVoConvertor;
import org.chiu.micro.exhibit.convertor.BlogExhibitVoConvertor;
import org.chiu.micro.exhibit.convertor.BlogHotReadVoConvertor;
import org.chiu.micro.exhibit.convertor.VisitStatisticsVoConvertor;
import org.chiu.micro.exhibit.dto.BlogDescriptionDto;
import org.chiu.micro.exhibit.dto.BlogEntityDto;
import org.chiu.micro.exhibit.dto.BlogExhibitDto;
import org.chiu.micro.exhibit.dto.BlogSensitiveContentDto;
import org.chiu.micro.exhibit.dto.SensitiveContent;
import org.chiu.micro.exhibit.vo.BlogDescriptionVo;
import org.chiu.micro.exhibit.vo.BlogExhibitVo;
import org.chiu.micro.exhibit.vo.BlogHotReadVo;
import org.chiu.micro.exhibit.vo.VisitStatisticsVo;
import org.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import org.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.chiu.micro.exhibit.lang.StatusEnum;
import org.chiu.micro.exhibit.service.BlogService;
import org.chiu.micro.exhibit.utils.SensitiveUtils;
import org.chiu.micro.exhibit.exception.MissException;
import org.chiu.micro.exhibit.page.PageAdapter;
import org.chiu.micro.exhibit.rpc.wrapper.BlogHttpServiceWrapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.chiu.micro.exhibit.lang.Const.*;
import static org.chiu.micro.exhibit.lang.ExceptionMessage.*;


/**
 * @author mingchiuli
 * @create 2022-11-27 2:10 pm
 */
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogSensitiveWrapper blogSensitiveWrapper;

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final StringRedisTemplate redisTemplate;

    private final BlogWrapper blogWrapper;

    private final ResourceLoader resourceLoader;

    private String visitScript;

    private String countYearsScript;

    @Value("${blog.highest-role}")
    private String highestRole;

    @PostConstruct
    @SneakyThrows
    private void init() {
        Resource visitResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/visit.lua");
        Resource countYearsResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/count-years.lua");
        visitScript = visitResource.getContentAsString(StandardCharsets.UTF_8);
        countYearsScript = countYearsResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public PageAdapter<BlogDescriptionVo> findPage(Integer currentPage, Integer year) {
        PageAdapter<BlogDescriptionDto> dtoPageAdapter = blogWrapper.findPage(currentPage, year);
        List<BlogDescriptionDto> descList = dtoPageAdapter.getContent();
        List<BlogDescriptionDto> descSensitiveList = new ArrayList<>();

        for (BlogDescriptionDto desc : descList) {
            Integer status = desc.getStatus();
            Long blogId = desc.getId();
            if (!StatusEnum.SENSITIVE_FILTER.getCode().equals(status)) {
                descSensitiveList.add(desc);
                continue;
            }

            BlogSensitiveContentDto sensitiveContentDto = blogSensitiveWrapper.findSensitiveByBlogId(blogId);
            List<SensitiveContent> words = sensitiveContentDto.getSensitiveContent();
            if (words.isEmpty()) {
                descSensitiveList.add(desc);
            } else {
                BlogDescriptionDto blogDescriptionDto = SensitiveUtils.deal(words, desc);
                descSensitiveList.add(blogDescriptionDto);
            }
        }

        dtoPageAdapter.setContent(descSensitiveList);
        return BlogDescriptionVoConvertor.convert(dtoPageAdapter);
    }

    @Override
    public Boolean checkToken(Long blogId, String token) {
        token = token.trim();
        String password = redisTemplate.opsForValue().get(READ_TOKEN.getInfo() + blogId);
        if (StringUtils.hasLength(token) && StringUtils.hasLength(password)) {
            return password.equals(token);
        }
        return false;
    }

    @Override
    public Integer getBlogStatus(List<String> roles, Long blogId, Long userId) {
        Integer status = blogWrapper.findStatusById(blogId);

        if (StatusEnum.NORMAL.getCode().equals(status) || StatusEnum.SENSITIVE_FILTER.getCode().equals(status)) {
            return status;
        }

        if (roles.isEmpty()) {
            return StatusEnum.HIDE.getCode();
        }

        if (roles.contains(highestRole)) {
            return StatusEnum.NORMAL.getCode();
        }

        BlogEntityDto blog = blogHttpServiceWrapper.findById(blogId);

        Long id = blog.getUserId();
        return Objects.equals(id, userId) ?
                StatusEnum.NORMAL.getCode() :
                StatusEnum.HIDE.getCode();
    }

    @Override
    public BlogExhibitVo getLockedBlog(Long blogId, String token) {
        boolean valid = checkToken(blogId, token);
        if (!valid) {
            throw new MissException(TOKEN_INVALID.getMsg());
        }
        
        blogWrapper.setReadCount(blogId);
        BlogExhibitDto blogExhibitDto = blogWrapper.findById(blogId);
        redisTemplate.delete(READ_TOKEN.getInfo() + blogId);
        return BlogExhibitVoConvertor.convert(blogExhibitDto);
    }

    @Override
    public List<Integer> searchYears() {
        Long count = Optional
                .ofNullable(
                        redisTemplate.execute(RedisScript.of(countYearsScript, Long.class),
                                List.of(BLOOM_FILTER_YEARS.getInfo())))
                .orElse(0L);
        int start = 2021;
        int end = Math.max(start + count.intValue() - 1, start);
        var years = new ArrayList<Integer>(end - start + 1);
        for (int year = start; year <= end; year++) {
            years.add(year);
        }
        if (years.size() == 1) {
            years.add(start);
        }
        return years;
    }

    @Override
    @SuppressWarnings("unchecked")
    public VisitStatisticsVo getVisitStatistics() {
        List<Long> list = Optional.ofNullable(redisTemplate.execute(RedisScript.of(visitScript,List.class),
                        List.of(DAY_VISIT.getInfo(), WEEK_VISIT.getInfo(), MONTH_VISIT.getInfo(), YEAR_VISIT.getInfo())))
                .orElseGet(ArrayList::new);

        return VisitStatisticsVoConvertor.convert(list);
    }

    @Override
    public List<BlogHotReadVo> getScoreBlogs() {
        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_READ.getInfo(), 0, 4);

        List<Long> ids = Optional.ofNullable(set).orElseGet(LinkedHashSet::new).stream()
                .map(item -> Long.valueOf(Optional.ofNullable(item.getValue()).orElse("0")))
                .toList();

        List<BlogEntityDto> blogs = blogHttpServiceWrapper.findAllById(ids);

        return BlogHotReadVoConvertor.convert(blogs, set);
    }

    @Override
    public BlogExhibitVo getBlogDetail(List<String> roles, Long id, Long userId) {

        BlogExhibitDto blogExhibitDto = blogWrapper.findById(id);
        Integer status = blogWrapper.findStatusById(id);

        if (StatusEnum.HIDE.getCode().equals(status) &&
                !roles.contains(highestRole) &&
                !Objects.equals(userId, blogExhibitDto.getUserId())) {
            throw new MissException(AUTH_EXCEPTION.getMsg());
        }

        if (StatusEnum.SENSITIVE_FILTER.getCode().equals(status) &&
                !roles.contains(highestRole) &&
                !Objects.equals(userId, blogExhibitDto.getUserId())) {
            BlogSensitiveContentDto sensitiveContentDto = blogSensitiveWrapper.findSensitiveByBlogId(id);
            List<SensitiveContent> words = sensitiveContentDto.getSensitiveContent();
            if (!words.isEmpty()) {
                blogExhibitDto = SensitiveUtils.deal(words, blogExhibitDto);
            }
        }

        blogWrapper.setReadCount(id);
        return BlogExhibitVoConvertor.convert(blogExhibitDto);
    }

    @Override
    public List<Integer> getYears() {
        return blogHttpServiceWrapper.getYears();
    }


    @Override
    public Long count() {
        return blogHttpServiceWrapper.count();
    }

    @Override
    public Long getCountByYear(Integer year) {
        return blogWrapper.getCountByYear(year);
    }

    @Override
    public List<Long> findIds(Integer pageNo, Integer pageSize) {
        return blogHttpServiceWrapper.findIds(pageNo, pageSize);
    }
}
