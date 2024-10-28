package wiki.chiu.micro.blog.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import wiki.chiu.micro.blog.constant.BlogOperateEnum;
import wiki.chiu.micro.blog.constant.BlogOperateMessage;
import wiki.chiu.micro.blog.convertor.BlogDeleteVoConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityRpcVoConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityVoConvertor;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.event.BlogOperateEvent;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.rpc.SearchHttpServiceWrapper;
import wiki.chiu.micro.blog.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.utils.AuthUtils;
import wiki.chiu.micro.blog.utils.OssSignUtils;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.blog.wrapper.BlogSensitiveWrapper;

import wiki.chiu.micro.common.dto.BlogSearchRpcDto;
import wiki.chiu.micro.common.dto.UserEntityRpcDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static wiki.chiu.micro.common.lang.Const.*;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;
import static wiki.chiu.micro.common.lang.StatusEnum.HIDE;

@Service
public class BlogServiceImpl implements BlogService {

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final OssHttpService ossHttpService;

    private final OssSignUtils ossSignUtils;

    private final ApplicationContext applicationContext;

    private final BlogRepository blogRepository;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final ResourceLoader resourceLoader;

    private final BlogSensitiveWrapper blogSensitiveWrapper;

    private final BlogSensitiveContentRepository blogSensitiveContentRepository;

    private final SearchHttpServiceWrapper searchHttpServiceWrapper;

    private final ExecutorService taskExecutor;

    @Value("${megalith.blog.highest-role}")
    private String highestRole;

    @Value("${megalith.blog.read.page-prefix}")
    private String readPrefix;


    @Value("${megalith.blog.oss.base-url}")
    private String baseUrl;

    private String hotBlogsScript;

    private String listDeleteScript;

    private String recoverDeleteScript;

    public BlogServiceImpl(UserHttpServiceWrapper userHttpServiceWrapper, OssHttpService ossHttpService, OssSignUtils ossSignUtils, ApplicationContext applicationContext, BlogRepository blogRepository, StringRedisTemplate redisTemplate, ObjectMapper objectMapper, ResourceLoader resourceLoader, BlogSensitiveWrapper blogSensitiveWrapper, BlogSensitiveContentRepository blogSensitiveContentRepository, SearchHttpServiceWrapper searchHttpServiceWrapper, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.ossHttpService = ossHttpService;
        this.ossSignUtils = ossSignUtils;
        this.applicationContext = applicationContext;
        this.blogRepository = blogRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.blogSensitiveWrapper = blogSensitiveWrapper;
        this.blogSensitiveContentRepository = blogSensitiveContentRepository;
        this.searchHttpServiceWrapper = searchHttpServiceWrapper;
        this.taskExecutor = taskExecutor;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource hotBlogsResource = resourceLoader
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/hot-blogs.lua");
        hotBlogsScript = hotBlogsResource.getContentAsString(StandardCharsets.UTF_8);
        Resource listDeleteResource = resourceLoader
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/list-delete.lua");
        listDeleteScript = listDeleteResource.getContentAsString(StandardCharsets.UTF_8);
        Resource recoverDeleteResource = resourceLoader
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/recover-delete.lua");
        recoverDeleteScript = recoverDeleteResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public void download(HttpServletResponse response) {
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            Set<BlogEntity> items = Collections.newSetFromMap(new ConcurrentHashMap<>());
            List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
            long total = blogRepository.count();
            int pageSize = 20;
            int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);

            for (int i = 1; i <= totalPage; i++) {
                PageRequest pageRequest = PageRequest.of(i, pageSize);
                CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                    Page<BlogEntity> page = blogRepository.findAll(pageRequest);
                    items.addAll(page.getContent());
                }, taskExecutor);
                completableFutures.add(completableFuture);
            }

            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);
            BlogEntity[] blogs = items.toArray(new BlogEntity[0]);
            int len = blogs.length;

            for (int i = 0; i < len; i++) {
                if (i == 0) {
                    // [
                    outputStream.write(new byte[]{91});
                }

                byte[] bytes = objectMapper.writeValueAsBytes(blogs[i]);
                outputStream.write(bytes);
                if (i != len - 1) {
                    // ,
                    outputStream.write(new byte[]{44});
                }

                if (i == len - 1) {
                    // ]
                    outputStream.write(new byte[]{93});
                }
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new MissException(e.getMessage());
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SseEmitter uploadOss(MultipartFile file, Long userId) {
        byte[] imageBytes;
        try {
            imageBytes = file.getBytes();
        } catch (IOException e) {
            throw new MissException(e.getMessage());
        }
        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElseGet(() -> UUID.randomUUID().toString())
                .replace(" ", "");
        if (imageBytes.length == 0) {
            throw new MissException(UPLOAD_MISS.getMsg());
        }

        var sseEmitter = new SseEmitter();
        taskExecutor.execute(() -> {
            String uuid = UUID.randomUUID().toString();

            UserEntityRpcDto user = userHttpServiceWrapper.findById(userId);
            String objectName = user.nickname() + "/" + uuid + "-" + originalFilename;

            Map<String, String> headers = new HashMap<>();
            String gmtDate = ossSignUtils.getGMTDate();
            headers.put(HttpHeaders.DATE, gmtDate);
            headers.put(HttpHeaders.AUTHORIZATION, ossSignUtils.getAuthorization(objectName, HttpMethod.PUT.name(), "image/jpg"));

            headers.put(HttpHeaders.CACHE_CONTROL, "no-cache");
            headers.put(HttpHeaders.CONTENT_TYPE, "image/jpg");
            ossHttpService.putOssObject(objectName, imageBytes, headers);
            // https://bloglmc.oss-cn-hangzhou.aliyuncs.com/admin/42166d224f4a20a45eca28b691529822730ed0ee.jpeg

            try {
                sseEmitter.send(baseUrl + "/" + objectName, MediaType.TEXT_PLAIN);
                sseEmitter.complete();
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        });
        return sseEmitter;
    }

    @Override
    @Async("commonExecutor")
    public void deleteOss(String url){
        String objectName = url.replace(baseUrl + "/", "");
        Map<String, String> headers = new HashMap<>();
        String gmtDate = ossSignUtils.getGMTDate();
        headers.put(HttpHeaders.DATE, gmtDate);
        headers.put(HttpHeaders.AUTHORIZATION, ossSignUtils.getAuthorization(objectName, HttpMethod.DELETE.name(), ""));

        ossHttpService.deleteOssObject(objectName, headers);
    }

    @Override
    public String setBlogToken(Long blogId, Long userId) {
        Long dbUserId = blogRepository.findUserIdById(blogId);

        if (!Objects.equals(userId, dbUserId)) {
            throw new MissException(USER_MISS.getMsg());
        }

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(READ_TOKEN + blogId, token, 24, TimeUnit.HOURS);
        return readPrefix + blogId + "?token=" + token;
    }

    @Override
    public void saveOrUpdate(BlogEntityReq blog, Long userId) {
        Optional<Long> blogId = blog.id();
        BlogEntity blogEntity;

        if (blogId.isPresent()) {
            blogEntity = blogRepository.findById(blogId.get())
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));

            AuthUtils.checkEditAuth(blogEntity, userId);
        } else {
            blogEntity = BlogEntity.builder()
                    .userId(userId)
                    .readCount(0L)
                    .build();
        }

        BlogEntityConvertor.convert(blog, blogEntity);

        List<BlogSensitiveContentEntity> blogSensitiveContentEntityList = blog.sensitiveContentList().stream()
                .distinct()
                .map(item -> BlogSensitiveContentEntity.builder()
                        .endIndex(item.endIndex())
                        .startIndex(item.startIndex())
                        .type(item.type())
                        .build())
                .toList();

        List<Long> existedSensitiveIds = Collections.emptyList();
        if (blogId.isPresent()) {
            existedSensitiveIds = blogSensitiveContentRepository.findByBlogId(blogId.get())
                    .stream()
                    .map(BlogSensitiveContentEntity::getId)
                    .toList();
        }

        BlogEntity saved = blogSensitiveWrapper.saveOrUpdate(blogEntity, blogSensitiveContentEntityList, existedSensitiveIds);

        // 通知消息给mq,更新并删除缓存
        BlogOperateEnum type = blogId.isPresent() ? BlogOperateEnum.UPDATE : BlogOperateEnum.CREATE;
        var blogSearchIndexMessage = new BlogOperateMessage(saved.getId(), type, blogEntity.getCreated().getYear());
        applicationContext.publishEvent(new BlogOperateEvent(this, blogSearchIndexMessage));
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageAdapter<BlogEntityVo> findAllBlogs(Integer currentPage, Integer size, String keywords) {

        BlogSearchRpcDto dto = searchHttpServiceWrapper.searchBlogs(currentPage, size, keywords);
        List<Long> ids = dto.ids();
        if (ids.isEmpty()) {
            return PageAdapter.emptyPage();
        }

        List<BlogEntity> items = blogRepository.findAllById(ids).stream()
                .sorted(Comparator.comparing(item -> ids.indexOf(item.getId())))
                .toList();

        List<BlogSensitiveContentEntity> blogSensitiveContentEntities = blogSensitiveContentRepository.findByBlogIdIn(ids);

        List<String> res = Optional.ofNullable(
                        redisTemplate.execute(RedisScript.of(hotBlogsScript, List.class),
                                Collections.singletonList(HOT_READ),
                                JsonUtils.writeValueAsString(ids.stream()
                                        .map(String::valueOf)
                                        .toList())))
                .orElseGet(Collections::emptyList);

        Map<Long, Integer> readMap = new HashMap<>();
        for (int i = 0; i < res.size(); i += 2) {
            readMap.put(Long.valueOf(res.get(i)), Integer.valueOf(res.get(i + 1)));
        }

        return BlogEntityVoConvertor.convert(items, readMap, blogSensitiveContentEntities, dto);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageAdapter<BlogDeleteVo> findDeletedBlogs(Integer currentPage, Integer size, Long userId) {

        List<String> deletedBlogsStr = redisTemplate.opsForList().range(QUERY_DELETED + userId, 0, -1);
        List<BlogEntity> deletedBlogs = Optional.ofNullable(deletedBlogsStr)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(blogStr -> JsonUtils.readValue(blogStr, BlogEntity.class))
                .toList();

        if (deletedBlogs.isEmpty()) {
            return PageAdapter.emptyPage();
        }

        int l = 0;
        for (BlogEntity blog : deletedBlogs) {
            if (LocalDateTime.now().minusDays(7).isAfter(blog.getUpdated())) {
                l++;
            } else {
                break;
            }
        }

        int start = (currentPage - 1) * size;

        List<String> resp = redisTemplate.execute(RedisScript.of(listDeleteScript, List.class),
                Collections.singletonList(QUERY_DELETED + userId),
                String.valueOf(l), "-1", String.valueOf(size - 1), String.valueOf(start));

        resp = Optional.ofNullable(resp).orElseGet(Collections::emptyList);
        List<String> respList = resp.subList(0, resp.size() - 1);
        Long total = Long.valueOf(resp.getLast());

        List<BlogEntity> list = respList.stream()
                .map(str -> JsonUtils.readValue(str, BlogEntity.class))
                .toList();

        return BlogDeleteVoConvertor.convert(l, list, currentPage, size, total);
    }

    @Override
    public void recoverDeletedBlog(Integer idx, Long userId) {

        String str = redisTemplate.execute(RedisScript.of(recoverDeleteScript, String.class),
                Collections.singletonList(QUERY_DELETED + userId),
                String.valueOf(idx));

        if (!StringUtils.hasLength(str)) {
            return;
        }

        BlogEntity tempBlog = JsonUtils.readValue(str, BlogEntity.class);
        tempBlog.setStatus(HIDE.getCode());
        BlogEntity blog = blogRepository.save(tempBlog);

        var blogSearchIndexMessage = new BlogOperateMessage(blog.getId(), BlogOperateEnum.CREATE, blog.getCreated().getYear());
        applicationContext.publishEvent(new BlogOperateEvent(this, blogSearchIndexMessage));
    }

    @Override
    public void deleteBatch(List<Long> ids, Long userId, List<String> roles) {

        List<BlogEntity> entities = blogRepository.findAllById(ids).stream()
                .filter(blogEntity -> Objects.equals(blogEntity.getUserId(), userId) || roles.contains(highestRole))
                .toList();

        ids = entities.stream()
                .map(BlogEntity::getId)
                .toList();

        List<Long> sensitiveIds = blogSensitiveContentRepository.findByBlogIdIn(ids).stream()
                .map(BlogSensitiveContentEntity::getId)
                .toList();
        blogSensitiveWrapper.deleteByIds(ids, sensitiveIds);

        entities.forEach(entity -> {
            var blogSearchIndexMessage = new BlogOperateMessage(entity.getId(), BlogOperateEnum.REMOVE, entity.getCreated().getYear());
            applicationContext.publishEvent(new BlogOperateEvent(this, blogSearchIndexMessage));
        });
    }

    @Override
    public BlogEntityRpcVo findById(Long blogId) {
        BlogEntity blogEntity = blogRepository.findById(blogId)
                .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        return BlogEntityRpcVoConvertor.convert(blogEntity);
    }

    @Override
    public BlogEntityRpcVo findByIdAndUserId(Long blogId, Long userId) {
        BlogEntity blogEntity = blogRepository.findById(blogId)
                .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        if (!Objects.equals(userId, blogEntity.getUserId())) {
            throw new MissException(NO_FOUND.getMsg());
        }
        return BlogEntityRpcVoConvertor.convert(blogEntity);
    }

    @Override
    public List<BlogEntityRpcVo> findAllById(List<Long> ids) {
        List<BlogEntity> blogEntities = blogRepository.findAllById(ids);
        return BlogEntityRpcVoConvertor.convert(blogEntities);
    }

    @Override
    public List<Integer> getYears() {
        return blogRepository.getYears();
    }

    @Override
    public Long count() {
        return blogRepository.count();
    }

    @Override
    public List<Long> findIds(Integer pageNo, Integer pageSize) {
        var pageRequest = PageRequest.of(pageNo - 1,
                pageSize);
        return blogRepository.findIds(pageRequest);
    }

    @Override
    public void setReadCount(Long blogId) {
        blogRepository.setReadCount(blogId);
    }

    @Override
    public Integer findStatusById(Long blogId) {
        return blogRepository.findStatusById(blogId);
    }

    @Override
    public PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize) {
        var pageRequest = PageRequest.of(pageNo - 1,
                pageSize,
                Sort.by("created").descending());

        Page<BlogEntity> page = blogRepository.findAll(pageRequest);
        return BlogEntityRpcVoConvertor.convert(page);
    }

    @Override
    public PageAdapter<BlogEntityRpcVo> findPageByCreatedBetween(Integer pageNo, Integer pageSize, LocalDateTime start, LocalDateTime end) {
        var pageRequest = PageRequest.of(pageNo - 1,
                pageSize,
                Sort.by("created").descending());
        Page<BlogEntity> page = blogRepository.findAllByCreatedBetween(pageRequest, start, end);
        return BlogEntityRpcVoConvertor.convert(page);
    }

    @Override
    public Long countByCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return blogRepository.countByCreatedBetween(start, end);
    }

    @Override
    public Long getPageCountYear(LocalDateTime created, LocalDateTime start, LocalDateTime end) {
        return blogRepository.getPageCountYear(created, start, end);
    }

    @Override
    public Long countByCreatedGreaterThanEqual(LocalDateTime created) {
        return blogRepository.countByCreatedGreaterThanEqual(created);
    }
}
