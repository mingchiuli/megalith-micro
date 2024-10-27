package wiki.chiu.micro.blog.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import wiki.chiu.micro.blog.convertor.BlogEditVoConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityDtoConvertor;
import wiki.chiu.micro.blog.dto.BlogEntityDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.req.BlogEditPushAllReq;
import wiki.chiu.micro.blog.service.BlogEditService;
import wiki.chiu.micro.blog.service.BlogSensitiveService;
import wiki.chiu.micro.blog.utils.AuthUtils;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogSensitiveContentVo;
import wiki.chiu.micro.blog.vo.SensitiveContentVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.utils.JsonUtils;
import wiki.chiu.micro.common.utils.KeyUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static wiki.chiu.micro.common.lang.Const.*;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;
import static wiki.chiu.micro.common.lang.MessageActionFieldEnum.*;

@Service
public class BlogEditServiceImpl implements BlogEditService {

    private final StringRedisTemplate redisTemplate;

    private String pushAllScript;

    private final ResourceLoader resourceLoader;

    private final BlogSensitiveService blogSensitiveService;

    private final BlogRepository blogRepository;

    private final TypeReference<List<SensitiveContentVo>> type = new TypeReference<>() {
    };

    public BlogEditServiceImpl(StringRedisTemplate redisTemplate, ResourceLoader resourceLoader, BlogSensitiveService blogSensitiveService, BlogRepository blogRepository) {
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
        this.blogSensitiveService = blogSensitiveService;
        this.blogRepository = blogRepository;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource pushAllResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/push-all.lua");
        pushAllScript = pushAllResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public void pushAll(BlogEditPushAllReq blog, Long userId) {
        Optional<Long> id = blog.id();
        Long originUserId;
        if (id.isPresent()) {
            BlogEntity blogEntity = blogRepository.findById(id.get())
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
            AuthUtils.checkEditAuth(blogEntity, userId);
            originUserId = blogEntity.getUserId();
        } else {
            originUserId = userId;
        }

        String redisKey = KeyUtils.createBlogEditRedisKey(originUserId, id.orElse(null));
        boolean exist = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        if (!exist) {
            return;
        }

        String content = blog.content();

        String[] splits = content.split(PARAGRAPH_SPLITTER);
        List<String> paragraphList = new ArrayList<>(splits.length + 2);
        Collections.addAll(paragraphList, splits);
        if (content.endsWith(PARAGRAPH_SPLITTER)) {
            paragraphList.add("");
        }
        String paragraphListString = JsonUtils.writeValueAsString(paragraphList);

        redisTemplate.execute(RedisScript.of(pushAllScript),
                Collections.singletonList(redisKey),
                paragraphListString, ID.getMsg(), USER_ID.getMsg(), TITLE.getMsg(), DESCRIPTION.getMsg(),
                STATUS.getMsg(), LINK.getMsg(), VERSION.getMsg(), SENSITIVE_CONTENT_LIST.getMsg(),
                id.map(Object::toString).orElse(""), originUserId.toString(), blog.title(),
                blog.description(), blog.status().toString(), blog.link(), blog.version().toString(), JsonUtils.writeValueAsString(blog.sensitiveContentList()),
                A_WEEK);
    }

    @Override
    public BlogEditVo findEdit(Long id, Long userId) {
        Long originUserId;
        if (id != null) {
            BlogEntity blogEntity = blogRepository.findById(id)
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
            AuthUtils.checkEditAuth(blogEntity, userId);
            originUserId = blogEntity.getUserId();
        } else {
            originUserId = userId;
        }

        String redisKey = KeyUtils.createBlogEditRedisKey(originUserId, id);
        Map<String, String> entries = redisTemplate.<String, String>opsForHash()
                .entries(redisKey);

        BlogEntityDto blog;
        List<SensitiveContentVo> sensitiveContentList;
        int version = -1;
        String paragraphListString = null;
        if (!entries.isEmpty()) {
            blog = BlogEntityConvertor.convert(entries);
            sensitiveContentList = JsonUtils.readValue(entries.get(SENSITIVE_CONTENT_LIST.getMsg()), type);
            version = Integer.parseInt(entries.get(VERSION.getMsg()));

            entries.remove(SENSITIVE_CONTENT_LIST.getMsg());
            entries.remove(ID.getMsg());
            entries.remove(USER_ID.getMsg());
            entries.remove(DESCRIPTION.getMsg());
            entries.remove(TITLE.getMsg());
            entries.remove(STATUS.getMsg());
            entries.remove(LINK.getMsg());
            entries.remove(VERSION.getMsg());

            StringBuilder content = new StringBuilder();

            for (int i = 1; i <= entries.size(); i++) {
                String idx = PARAGRAPH_PREFIX + i;
                content.append(entries.get(idx));
                if (i != entries.size()) {
                    content.append(PARAGRAPH_SPLITTER);
                }
            }

            blog = new BlogEntityDto(blog.id(), blog.userId(), blog.title(), blog.description(), content.toString(), null, null, blog.status(), blog.link(), blog.readCount());
        } else if (Objects.isNull(id)) {
            // 新文章
            blog = BlogEntityDto.builder()
                    .status(StatusEnum.NORMAL.getCode())
                    .userId(originUserId)
                    .content("")
                    .description("")
                    .link("")
                    .title("")
                    .build();
            paragraphListString = "[]";
            sensitiveContentList = new ArrayList<>();
        } else {
            BlogEntity userBlogEntity = blogRepository.findById(id)
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));

            blog = BlogEntityDtoConvertor.convert(userBlogEntity);
            List<String> paragraphList = List.of(blog.content().split(PARAGRAPH_SPLITTER));
            paragraphListString = JsonUtils.writeValueAsString(paragraphList);
            BlogSensitiveContentVo blogSensitiveContentVo = blogSensitiveService.findByBlogId(id);
            sensitiveContentList = blogSensitiveContentVo.sensitiveContent();
        }

        if (StringUtils.hasLength(paragraphListString)) {
            redisTemplate.execute(RedisScript.of(pushAllScript),
                    Collections.singletonList(redisKey),
                    paragraphListString, ID.getMsg(), USER_ID.getMsg(), TITLE.getMsg(), DESCRIPTION.getMsg(),
                    STATUS.getMsg(), LINK.getMsg(), VERSION.getMsg(), SENSITIVE_CONTENT_LIST.getMsg(),
                    Objects.isNull(blog.id()) ? "" : blog.id().toString(), originUserId.toString(), blog.title(),
                    blog.description(), blog.status().toString(), blog.link(), Integer.toString(version), JsonUtils.writeValueAsString(sensitiveContentList),
                    A_WEEK);
        }

        return BlogEditVoConvertor.convert(blog, version, sensitiveContentList);
    }
}