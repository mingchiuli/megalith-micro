package wiki.chiu.micro.blog.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import wiki.chiu.micro.blog.convertor.*;
import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.utils.EditAuthUtils;
import wiki.chiu.micro.common.lang.BlogOperateEnum;
import wiki.chiu.micro.common.lang.BlogOperateMessage;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.event.BlogOperateEvent;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.rpc.SearchHttpServiceWrapper;
import wiki.chiu.micro.blog.rpc.UserHttpServiceWrapper;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.common.lang.BlogStatusEnum;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.utils.OssSignUtils;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.blog.wrapper.BlogWrapper;

import wiki.chiu.micro.common.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.vo.BlogSearchRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.utils.JsonUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import wiki.chiu.micro.common.utils.SQLUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static wiki.chiu.micro.common.lang.Const.*;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

@Service
public class BlogServiceImpl implements BlogService {

    private static final String IMAGE_JPG = "image/jpg";

    private final UserHttpServiceWrapper userHttpServiceWrapper;

    private final OssHttpService ossHttpService;

    private final ApplicationContext applicationContext;

    private final BlogRepository blogRepository;

    private final StringRedisTemplate redisTemplate;

    private final ResourceLoader resourceLoader;

    private final BlogWrapper blogWrapper;

    private final BlogSensitiveContentRepository blogSensitiveContentRepository;

    private final SearchHttpServiceWrapper searchHttpServiceWrapper;

    private final ExecutorService taskExecutor;

    private final ObjectMapper objectMapper;

    @Value("${megalith.blog.highest-role}")
    private String highestRole;

    @Value("${megalith.blog.read.page-prefix}")
    private String readPrefix;

    @Value("${megalith.blog.oss.base-url}")
    private String baseUrl;

    @Value("${megalith.blog.aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${megalith.blog.aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${megalith.blog.aliyun.oss.bucket-name}")
    private String bucketName;

    private String hotBlogsScript;

    private String listDeleteScript;

    private String recoverDeleteScript;

    private String blogDeleteScript;

    public BlogServiceImpl(UserHttpServiceWrapper userHttpServiceWrapper, OssHttpService ossHttpService, ApplicationContext applicationContext, BlogRepository blogRepository, StringRedisTemplate redisTemplate, ResourceLoader resourceLoader, BlogWrapper blogWrapper, BlogSensitiveContentRepository blogSensitiveContentRepository, SearchHttpServiceWrapper searchHttpServiceWrapper, @Qualifier("commonExecutor") ExecutorService taskExecutor, ObjectMapper objectMapper) {
        this.userHttpServiceWrapper = userHttpServiceWrapper;
        this.ossHttpService = ossHttpService;
        this.applicationContext = applicationContext;
        this.blogRepository = blogRepository;
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
        this.blogWrapper = blogWrapper;
        this.blogSensitiveContentRepository = blogSensitiveContentRepository;
        this.searchHttpServiceWrapper = searchHttpServiceWrapper;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource hotBlogsResource = resourceLoader
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/hot-blogs.lua");
        hotBlogsScript = hotBlogsResource.getContentAsString(StandardCharsets.UTF_8);
        Resource listDeleteResource = resourceLoader
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/blog-delete-list.lua");
        listDeleteScript = listDeleteResource.getContentAsString(StandardCharsets.UTF_8);
        Resource recoverDeleteResource = resourceLoader
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/recover-delete.lua");
        recoverDeleteScript = recoverDeleteResource.getContentAsString(StandardCharsets.UTF_8);
        Resource blogDeleteResource = resourceLoader
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/rpush-expire.lua");
        blogDeleteScript = blogDeleteResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public void download(HttpServletResponse response, BlogDownloadReq downloadReq, Long userId, List<String> roles) {
        Set<BlogEntity> blogs = Collections.newSetFromMap(new ConcurrentHashMap<>());
        Set<BlogSensitiveContentEntity> blogSensitives = Collections.newSetFromMap(new ConcurrentHashMap<>());
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        BlogSysCountSearchReq blogSysCountSearchReq = BlogSysCountSearchReqConvertor.convert(downloadReq, userId, roles);

        Long total = searchHttpServiceWrapper.countBlogs(blogSysCountSearchReq);
        int pageSize = 20;
        int totalPage = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);

        for (int i = 1; i <= totalPage; i++) {
            BlogSysSearchReq req = BlogSysSearchReqConvertor.convert(downloadReq, i, pageSize, userId, roles);
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            RequestContextHolder.setRequestAttributes(servletRequestAttributes, true);
            completableFutures.add(fetchBlogsAsync(req, blogs, blogSensitives));
        }

        waitForFutures(completableFutures);

        writeResponse(response, blogs, blogSensitives);
    }

    private CompletableFuture<Void> fetchBlogsAsync(BlogSysSearchReq req, Set<BlogEntity> blogs, Set<BlogSensitiveContentEntity> blogSensitives) {
        return CompletableFuture.runAsync(() -> {
            BlogSearchRpcVo dto = searchHttpServiceWrapper.searchBlogs(req);
            List<Long> ids = dto.ids();
            blogSensitives.addAll(blogSensitiveContentRepository.findByBlogIdIn(ids));
            blogs.addAll(blogRepository.findAllById(ids).stream()
                    .sorted(Comparator.comparing(item -> ids.indexOf(item.getId())))
                    .toList());
        }, taskExecutor);
    }

    private void waitForFutures(List<CompletableFuture<Void>> futures) {
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeResponse(HttpServletResponse response, Set<BlogEntity> blogs, Set<BlogSensitiveContentEntity> blogSensitives) {
        try (var outputStream = response.getOutputStream()) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            String blog = SQLUtils.entityToInsertSQL(new ArrayList<>(blogs), Const.BLOG_TABLE);
            String blogSensitive = SQLUtils.entityToInsertSQL(new ArrayList<>(blogSensitives), BLOG_SENSITIVE_TABLE);

            byte[] bytes = SQLUtils.compose(blog, blogSensitive).getBytes();
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SseEmitter uploadOss(MultipartFile file, Long userId) {
        byte[] imageBytes = getImageBytes(file);
        String objectName = getObjectName(file, userId);

        SseEmitter sseEmitter = new SseEmitter();
        taskExecutor.execute(() -> uploadImageToOss(imageBytes, objectName, sseEmitter));
        return sseEmitter;
    }

    private byte[] getImageBytes(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            if (imageBytes.length == 0) {
                throw new MissException(UPLOAD_MISS.getMsg());
            }
            return imageBytes;
        } catch (IOException e) {
            throw new MissException(e.getMessage());
        }
    }

    private String getObjectName(MultipartFile file, Long userId) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElseGet(() -> UUID.randomUUID().toString())
                .replace(" ", "");
        String uuid = UUID.randomUUID().toString();
        UserEntityRpcVo user = userHttpServiceWrapper.findById(userId);
        return user.nickname() + "/" + uuid + "-" + originalFilename;
    }

    private void uploadImageToOss(byte[] imageBytes, String objectName, SseEmitter sseEmitter) {
        Map<String, String> headers = getOssHeaders(objectName, HttpMethod.PUT.name(), IMAGE_JPG);
        ossHttpService.putOssObject(objectName, imageBytes, headers);
        sendSseEmitter(sseEmitter, baseUrl + "/" + objectName);
    }

    private Map<String, String> getOssHeaders(String objectName, String method, String contentType) {
        Map<String, String> headers = new HashMap<>();
        String gmtDate = OssSignUtils.getGMTDate();
        headers.put(HttpHeaders.DATE, gmtDate);
        headers.put(HttpHeaders.AUTHORIZATION, OssSignUtils.getAuthorization(objectName, method, contentType, accessKeyId, accessKeySecret, bucketName));
        headers.put(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        return headers;
    }

    private void sendSseEmitter(SseEmitter sseEmitter, String url) {
        try {
            sseEmitter.send(url, MediaType.TEXT_PLAIN);
            sseEmitter.complete();
        } catch (IOException e) {
            sseEmitter.completeWithError(e);
        }
    }

    @Override
    public void deleteOss(String url) {
        String objectName = url.replace(baseUrl + "/", "");
        Map<String, String> headers = getOssHeaders(objectName, HttpMethod.DELETE.name(), "");
        taskExecutor.execute(() -> ossHttpService.deleteOssObject(objectName, headers));
    }

    @Override
    public String setBlogToken(Long blogId, Long userId) {
        validateUser(blogId, userId);
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(READ_TOKEN + blogId, token, 24, TimeUnit.HOURS);
        return readPrefix + blogId + "?token=" + token;
    }


    private void validateUser(Long blogId, Long userId) {
        Long dbUserId = blogRepository.findUserIdById(blogId);
        if (!Objects.equals(userId, dbUserId)) {
            throw new MissException(USER_MISS.getMsg());
        }
    }

    @Override
    public void saveOrUpdate(BlogEntityReq blog, Long userId, List<String> roles) {
        BlogEntity dealBlog = getBlogEntity(blog, userId, roles);
        BlogEntity blogEntity = BlogEntityConvertor.convert(blog, dealBlog);

        List<BlogSensitiveContentEntity> blogSensitiveContentEntityList = blog.sensitiveContentList().stream()
                .distinct()
                .map(item -> BlogSensitiveContentEntity.builder()
                        .endIndex(item.endIndex())
                        .startIndex(item.startIndex())
                        .type(item.type())
                        .build())
                .toList();

        List<Long> existedSensitiveIds = blog.id()
                .map(blogId -> blogSensitiveContentRepository.findByBlogId(blogId).stream()
                        .map(BlogSensitiveContentEntity::getId)
                        .toList())
                .orElse(Collections.emptyList());

        BlogEntity saved = blogWrapper.saveOrUpdate(blogEntity, blogSensitiveContentEntityList, existedSensitiveIds);

        taskExecutor.execute(() -> notifyBlogOperation(blog.id().isPresent() ? BlogOperateEnum.UPDATE : BlogOperateEnum.CREATE, saved));
    }

    private BlogEntity getBlogEntity(BlogEntityReq blog, Long userId, List<String> roles) {
        return blog.id()
                .map(blogId -> {
                    BlogEntity blogEntity = blogRepository.findById(blogId)
                            .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
                    EditAuthUtils.checkSaveAuth(blogEntity, userId, roles, highestRole);
                    return blogEntity;
                })
                .orElseGet(() -> BlogEntity.builder()
                        .userId(userId)
                        .readCount(0L)
                        .build());
    }

    private void notifyBlogOperation(BlogOperateEnum type, BlogEntity blogEntity) {
        var blogSearchIndexMessage = new BlogOperateMessage(blogEntity.getId(), type.getCode());
        applicationContext.publishEvent(new BlogOperateEvent(this, blogSearchIndexMessage));
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageAdapter<BlogEntityVo> findAllBlogs(BlogQueryReq blogQueryReq, Long userId, List<String> roles) {

        BlogSysSearchReq req = BlogSysSearchReqConvertor.convert(blogQueryReq, userId, roles);
        BlogSearchRpcVo dto = searchHttpServiceWrapper.searchBlogs(req);
        List<Long> ids = dto.ids();
        if (ids.isEmpty()) {
            return PageAdapter.emptyPage();
        }

        List<BlogEntity> items = blogRepository.findAllById(ids).stream()
                .sorted(Comparator.comparing(item -> ids.indexOf(item.getId())))
                .filter(item -> req.status() == null || Objects.equals(item.getStatus(), req.status()))
                .toList();

        List<BlogSensitiveContentEntity> blogSensitiveContentEntities = blogSensitiveContentRepository.findByBlogIdIn(ids);

        List<String> res = redisTemplate.execute(RedisScript.of(hotBlogsScript, List.class),
                Collections.singletonList(HOT_READ),
                JsonUtils.writeValueAsString(objectMapper, ids.stream()
                        .map(String::valueOf)
                        .toList()));

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
                .map(blogStr -> JsonUtils.readValue(objectMapper, blogStr, BlogEntity.class))
                .toList();

        if (deletedBlogs.isEmpty()) {
            return PageAdapter.emptyPage();
        }

        int l = (int) deletedBlogs.stream()
                .filter(blog -> LocalDateTime.now().minusDays(7).isAfter(blog.getUpdated()))
                .count();

        int start = (currentPage - 1) * size;

        List<String> resp = redisTemplate.execute(RedisScript.of(listDeleteScript, List.class),
                Collections.singletonList(QUERY_DELETED + userId),
                String.valueOf(l), "-1", String.valueOf(size - 1), String.valueOf(start));

        List<String> respList = resp.subList(0, resp.size() - 1);
        Long total = Long.valueOf(resp.getLast());

        List<BlogEntity> blogEntities = respList.stream()
                .map(str -> JsonUtils.readValue(objectMapper, str, BlogDeleteDto.class))
                .map(BlogEntityConvertor::convert)
                .toList();

        return BlogDeleteVoConvertor.convert(l, blogEntities, currentPage, size, total);
    }

    @Override
    public void recoverDeletedBlog(Integer idx, Long userId) {

        String str = redisTemplate.execute(RedisScript.of(recoverDeleteScript, String.class),
                Collections.singletonList(QUERY_DELETED + userId),
                String.valueOf(idx));

        if (!StringUtils.hasLength(str)) {
            return;
        }

        BlogDeleteDto delBlog = JsonUtils.readValue(objectMapper, str, BlogDeleteDto.class);
        BlogEntity tempBlog = BlogEntityConvertor.convertRecover(delBlog);
        BlogEntity blog = blogRepository.save(tempBlog);

        taskExecutor.execute(() -> notifyBlogOperation(BlogOperateEnum.CREATE, blog));
    }

    @Override
    public void deleteBatch(List<Long> ids, Long userId, List<String> roles) {

        List<BlogEntity> entities = blogRepository.findAllById(ids).stream()
                .filter(blogEntity -> Objects.equals(blogEntity.getUserId(), userId) || roles.contains(highestRole))
                .toList();

        List<Long> idList = entities.stream()
                .map(BlogEntity::getId)
                .toList();

        List<Long> sensitiveIds = blogSensitiveContentRepository.findByBlogIdIn(idList).stream()
                .map(BlogSensitiveContentEntity::getId)
                .toList();
        blogWrapper.deleteByIds(idList, sensitiveIds);

        taskExecutor.execute(() ->
                entities.forEach(entity -> {
                    redisTemplate.execute(RedisScript.of(blogDeleteScript),
                            Collections.singletonList(QUERY_DELETED + userId),
                            JsonUtils.writeValueAsString(objectMapper, BlogDeleteDtoConvertor.convert(entity)), A_WEEK);

                    notifyBlogOperation(BlogOperateEnum.REMOVE, entity);
                }));
    }

    @Override
    public BlogEntityRpcVo findById(Long blogId) {
        BlogEntity blogEntity = blogRepository.findById(blogId)
                .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        return BlogEntityRpcVoConvertor.convert(blogEntity);
    }

    @Override
    public List<BlogEntityRpcVo> findAllById(List<Long> ids) {
        List<BlogEntity> blogEntities = blogRepository.findAllById(ids);
        return BlogEntityRpcVoConvertor.convert(blogEntities);
    }

    @Override
    public Long count() {
        return blogRepository.count();
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

        List<Integer> statusList = List.of(BlogStatusEnum.NORMAL.getCode(), BlogStatusEnum.SENSITIVE_FILTER.getCode(), BlogStatusEnum.HIDE.getCode());

        Page<BlogEntity> page = blogRepository.findByStatusIn(pageRequest, statusList);
        return BlogEntityRpcVoConvertor.convert(page);
    }

    @Override
    public Long countByCreatedGreaterThanEqual(LocalDateTime created) {
        return blogRepository.countByCreatedGreaterThanEqual(created);
    }
}
