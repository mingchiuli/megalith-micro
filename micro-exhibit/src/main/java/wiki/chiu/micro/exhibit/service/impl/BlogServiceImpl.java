package wiki.chiu.micro.exhibit.service.impl;

import jakarta.annotation.PostConstruct;
import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.dto.BlogSensitiveContentRpcDto;
import wiki.chiu.micro.common.dto.SensitiveContentRpcDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.convertor.BlogDescriptionVoConvertor;
import wiki.chiu.micro.exhibit.convertor.BlogExhibitVoConvertor;
import wiki.chiu.micro.exhibit.convertor.BlogHotReadVoConvertor;
import wiki.chiu.micro.exhibit.convertor.VisitStatisticsVoConvertor;
import wiki.chiu.micro.exhibit.dto.*;
import wiki.chiu.micro.exhibit.rpc.BlogHttpServiceWrapper;
import wiki.chiu.micro.exhibit.service.BlogService;
import wiki.chiu.micro.exhibit.utils.SensitiveUtils;
import wiki.chiu.micro.exhibit.vo.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;
import wiki.chiu.micro.exhibit.wrapper.BlogSensitiveWrapper;
import wiki.chiu.micro.exhibit.wrapper.BlogWrapper;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static wiki.chiu.micro.common.lang.Const.*;
import static wiki.chiu.micro.common.lang.ExceptionMessage.AUTH_EXCEPTION;
import static wiki.chiu.micro.common.lang.ExceptionMessage.TOKEN_INVALID;


/**
 * @author mingchiuli
 * @create 2022-11-27 2:10 pm
 */
@Service
public class BlogServiceImpl implements BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);
    private final BlogSensitiveWrapper blogSensitiveWrapper;

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final RedissonClient redissonClient;

    private final BlogWrapper blogWrapper;

    private final ResourceLoader resourceLoader;

    private String visitScript;

    private String countYearsScript;

    @Value("${megalith.blog.highest-role}")
    private String highestRole;

    public BlogServiceImpl(BlogSensitiveWrapper blogSensitiveWrapper, BlogHttpServiceWrapper blogHttpServiceWrapper, RedissonClient redissonClient, BlogWrapper blogWrapper, ResourceLoader resourceLoader) {
        this.blogSensitiveWrapper = blogSensitiveWrapper;
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.redissonClient = redissonClient;
        this.blogWrapper = blogWrapper;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource visitResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/visit.lua");
        Resource countYearsResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/count-years.lua");
        visitScript = visitResource.getContentAsString(StandardCharsets.UTF_8);
        countYearsScript = countYearsResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public PageAdapter<BlogDescriptionVo> findPage(Integer currentPage, Integer year) {
        PageAdapter<BlogDescriptionDto> dtoPageAdapter = blogWrapper.findPage(currentPage, year);
        List<BlogDescriptionDto> descList = dtoPageAdapter.content();

        List<BlogDescriptionDto> descSensitiveList = descList.stream()
                .map(desc -> {
                    if (!BlogStatusEnum.SENSITIVE_FILTER.getCode().equals(desc.status())) {
                        return desc;
                    }
                    List<SensitiveContentRpcDto> words = blogSensitiveWrapper.findSensitiveByBlogId(desc.id()).sensitiveContent();
                    if (words.isEmpty()) {
                        return desc;
                    }
                    return SensitiveUtils.deal(words, desc);
                })
                .toList();

        dtoPageAdapter = new PageAdapter<>(descSensitiveList, dtoPageAdapter);

        return BlogDescriptionVoConvertor.convert(dtoPageAdapter);
    }

    @Override
    public Boolean checkToken(Long blogId, String token) {
        token = token.trim();
        Object password = redissonClient.getBucket(READ_TOKEN + blogId).get();
        if (StringUtils.hasLength(token) && password != null) {
            return Objects.equals(password, token);
        }
        return false;
    }

    @Override
    public Integer getBlogStatus(List<String> roles, Long blogId, Long userId) {
        Integer status = blogWrapper.findStatusById(blogId);

        if (BlogStatusEnum.NORMAL.getCode().equals(status) || BlogStatusEnum.SENSITIVE_FILTER.getCode().equals(status)) {
            return status;
        }

        if (roles.isEmpty()) {
            return BlogStatusEnum.HIDE.getCode();
        }

        if (roles.contains(highestRole)) {
            return BlogStatusEnum.NORMAL.getCode();
        }

        BlogEntityRpcDto blog = blogHttpServiceWrapper.findById(blogId);

        Long id = blog.userId();
        return Objects.equals(id, userId) ?
                BlogStatusEnum.NORMAL.getCode() :
                BlogStatusEnum.HIDE.getCode();
    }

    @Override
    public BlogExhibitVo getLockedBlog(Long blogId, String token) {
        boolean valid = checkToken(blogId, token);
        if (!valid) {
            throw new MissException(TOKEN_INVALID.getMsg());
        }

        blogWrapper.setReadCount(blogId);
        BlogExhibitDto blogExhibitDto = blogWrapper.findById(blogId);
        redissonClient.getBucket(READ_TOKEN + blogId).delete();
        return BlogExhibitVoConvertor.convert(blogExhibitDto);
    }

    @Override
    public List<Integer> searchYears() {

        Long count = redissonClient.getScript().eval(Mode.READ_ONLY, countYearsScript, ReturnType.INTEGER, List.of(BLOOM_FILTER_YEARS));
        int start = 2021;
        int end = (int) Math.max(start + count - 1, start);
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
    public VisitStatisticsVo getVisitStatistics() {
        List<Long> list = redissonClient.getScript().eval(Mode.READ_ONLY, visitScript, ReturnType.MULTI, List.of(DAY_VISIT, WEEK_VISIT, MONTH_VISIT, YEAR_VISIT));
        return VisitStatisticsVoConvertor.convert(list);
    }

    @Override
    public List<BlogHotReadVo> getScoreBlogs() {
        Collection<ScoredEntry<String>> scoredEntries = redissonClient.<String>getScoredSortedSet(HOT_READ).entryRangeReversed(0, 4);

        List<Long> ids = scoredEntries.stream()
                .map(ScoredEntry::getValue)
                .map(Long::valueOf)
                .toList();

        List<BlogEntityRpcDto> blogs = blogHttpServiceWrapper.findAllById(ids);

        return BlogHotReadVoConvertor.convert(blogs, scoredEntries);
    }

    @Override
    public BlogExhibitVo getBlogDetail(List<String> roles, Long id, Long userId) {

        BlogExhibitDto blogExhibitDto = blogWrapper.findById(id);
        Integer status = blogWrapper.findStatusById(id);

        if (BlogStatusEnum.HIDE.getCode().equals(status) &&
                !roles.contains(highestRole) &&
                !Objects.equals(userId, blogExhibitDto.userId())) {
            throw new MissException(AUTH_EXCEPTION.getMsg());
        }

        if (BlogStatusEnum.SENSITIVE_FILTER.getCode().equals(status) &&
                !roles.contains(highestRole) &&
                !Objects.equals(userId, blogExhibitDto.userId())) {
            BlogSensitiveContentRpcDto sensitiveContentDto = blogSensitiveWrapper.findSensitiveByBlogId(id);
            List<SensitiveContentRpcDto> words = sensitiveContentDto.sensitiveContent();
            if (!words.isEmpty()) {
                blogExhibitDto = SensitiveUtils.deal(words, blogExhibitDto);
            }
        }

        blogWrapper.setReadCount(id);
        return BlogExhibitVoConvertor.convert(blogExhibitDto);
    }
}
