package wiki.chiu.micro.blog.service.impl;

import static wiki.chiu.micro.common.lang.Const.*;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.blog.convertor.*;
import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.service.BlogAccessPolicy;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.service.port.BlogSearchGateway;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.blog.wrapper.BlogWrapper;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.*;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;

@Service
public class BlogServiceImpl implements BlogService {

  private final BlogRepository blogRepository;

  private final StringRedisTemplate redisTemplate;

  private final ResourceLoader resourceLoader;

  private final BlogWrapper blogWrapper;

  private final BlogSensitiveContentRepository blogSensitiveContentRepository;

  private final BlogSearchGateway blogSearch;

  private final JsonMapper jsonMapper;

  private final BlogAccessPolicy accessPolicy;

  private String hotBlogsScript;

  private String listDeleteScript;

  public BlogServiceImpl(
      BlogRepository blogRepository,
      StringRedisTemplate redisTemplate,
      ResourceLoader resourceLoader,
      BlogWrapper blogWrapper,
      BlogSensitiveContentRepository blogSensitiveContentRepository,
      BlogSearchGateway blogSearch,
      JsonMapper jsonMapper,
      BlogAccessPolicy accessPolicy) {
    this.blogRepository = blogRepository;
    this.redisTemplate = redisTemplate;
    this.resourceLoader = resourceLoader;
    this.blogWrapper = blogWrapper;
    this.blogSensitiveContentRepository = blogSensitiveContentRepository;
    this.blogSearch = blogSearch;
    this.jsonMapper = jsonMapper;
    this.accessPolicy = accessPolicy;
  }

  @PostConstruct
  private void init() throws IOException {
    Resource hotBlogsResource =
        resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/hot-blogs.lua");
    hotBlogsScript = hotBlogsResource.getContentAsString(StandardCharsets.UTF_8);
    Resource listDeleteResource =
        resourceLoader.getResource(
            ResourceUtils.CLASSPATH_URL_PREFIX + "script/blog-delete-list.lua");
    listDeleteScript = listDeleteResource.getContentAsString(StandardCharsets.UTF_8);
  }

  @Override
  public BlogEditVo findEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions) {

    BlogEntity blog;
    List<BlogEditVo.SensitiveContentVo> sensitiveContentList;
    if (id != null) {
      blog = blogRepository.findById(id).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
      accessPolicy.requireCollaboration(blog, userId, dataPermissions);
      var sensitiveContentRpcList = blogSensitiveContentRepository.findByBlogId(id);
      sensitiveContentList = SensitiveContentVoConvertor.convert(sensitiveContentRpcList);
    } else {
      blog = createNewBlog(userId);
      sensitiveContentList = new ArrayList<>();
    }

    return BlogEditVoConvertor.convert(
        blog, sensitiveContentList, accessPolicy.permissions(blog, userId, dataPermissions));
  }

  private BlogEntity createNewBlog(Long userId) {
    return BlogEntity.builder()
        .userId(userId)
        .status(BlogStatusEnum.NORMAL.getCode())
        .content("")
        .description("")
        .link("")
        .title("")
        .build();
  }

  @Override
  public void saveOrUpdate(
      BlogEntityReq blog, Long userId, List<DataPermissionEnum> dataPermissions) {
    List<BlogSensitiveContentEntity> blogSensitiveContentEntityList =
        blog.sensitiveContentList().stream()
            .distinct()
            .map(
                item ->
                    BlogSensitiveContentEntity.builder()
                        .endIndex(item.endIndex())
                        .startIndex(item.startIndex())
                        .type(item.type())
                        .build())
            .toList();
    blogWrapper.saveOrUpdate(blog, userId, dataPermissions, blogSensitiveContentEntityList);
  }

  @Override
  @SuppressWarnings("unchecked")
  public PageAdapter<BlogEntityVo> findAllBlogs(
      BlogQueryReq blogQueryReq, Long userId, List<DataPermissionEnum> dataPermissions) {

    BlogSysSearchReq req =
        BlogSysSearchReqConvertor.convert(
            blogQueryReq, userId, dataPermissions.contains(DataPermissionEnum.BLOG_VIEW_ALL));
    BlogSearchRpcVo dto = blogSearch.searchBlogs(req);
    List<Long> ids = dto.ids();
    if (ids.isEmpty()) {
      return PageAdapter.emptyPage();
    }

    List<BlogEntity> items =
        blogRepository.findAllById(ids).stream()
            .sorted(Comparator.comparing(item -> ids.indexOf(item.getId())))
            .filter(item -> req.status() == null || Objects.equals(item.getStatus(), req.status()))
            .toList();

    List<BlogSensitiveContentEntity> blogSensitiveContentEntities =
        blogSensitiveContentRepository.findByBlogIdIn(ids);

    List<String> res =
        redisTemplate.execute(
            RedisScript.of(hotBlogsScript, List.class),
            Collections.singletonList(HOT_READ),
            jsonMapper.writeValueAsString(ids.stream().map(String::valueOf).toList()));

    Map<Long, Integer> readMap = new HashMap<>();
    for (int i = 0; i < res.size(); i += 2) {
      readMap.put(Long.valueOf(res.get(i)), Integer.valueOf(res.get(i + 1)));
    }

    return BlogEntityVoConvertor.convert(items, readMap, blogSensitiveContentEntities, dto);
  }

  @Override
  @SuppressWarnings("unchecked")
  public PageAdapter<BlogDeleteVo> findDeletedBlogs(
      Integer currentPage, Integer size, Long userId) {

    List<String> deletedBlogsStr = redisTemplate.opsForList().range(QUERY_DELETED + userId, 0, -1);
    List<BlogEntity> deletedBlogs =
        Optional.ofNullable(deletedBlogsStr).orElseGet(Collections::emptyList).stream()
            .map(blogStr -> jsonMapper.readValue(blogStr, BlogEntity.class))
            .toList();

    if (deletedBlogs.isEmpty()) {
      return PageAdapter.emptyPage();
    }

    int l =
        (int)
            deletedBlogs.stream()
                .filter(blog -> LocalDateTime.now().minusDays(7).isAfter(blog.getUpdated()))
                .count();

    int start = (currentPage - 1) * size;

    List<String> resp =
        redisTemplate.execute(
            RedisScript.of(listDeleteScript, List.class),
            Collections.singletonList(QUERY_DELETED + userId),
            String.valueOf(l),
            "-1",
            String.valueOf(size - 1),
            String.valueOf(start));

    List<String> respList = resp.subList(0, resp.size() - 1);
    Long total = Long.valueOf(resp.getLast());

    List<BlogEntity> blogEntities =
        respList.stream()
            .map(str -> jsonMapper.readValue(str, BlogDeleteDto.class))
            .map(BlogEntityConvertor::convert)
            .toList();

    return BlogDeleteVoConvertor.convert(l, blogEntities, currentPage, size, total);
  }

  @Override
  public void recoverDeletedBlog(Integer idx, Long userId) {
    String recycleKey = QUERY_DELETED + userId;
    String str = redisTemplate.opsForList().index(recycleKey, idx);

    if (!StringUtils.hasLength(str)) {
      return;
    }

    BlogDeleteDto delBlog = jsonMapper.readValue(str, BlogDeleteDto.class);
    blogWrapper.recoverDeletedBlog(delBlog, userId);
    redisTemplate.opsForList().remove(recycleKey, 1, str);
  }

  @Override
  public void deleteBatch(List<Long> ids, Long userId, List<DataPermissionEnum> dataPermissions) {
    blogWrapper.deleteByIds(ids, userId, dataPermissions);
  }
}
