package org.chiu.micro.blog.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import org.chiu.micro.blog.convertor.BlogEditVoConvertor;
import org.chiu.micro.blog.convertor.BlogEntityConvertor;
import org.chiu.micro.blog.convertor.BlogEntityDtoConvertor;
import org.chiu.micro.blog.dto.BlogEntityDto;
import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.exception.MissException;
import org.chiu.micro.blog.key.KeyFactory;
import org.chiu.micro.blog.lang.StatusEnum;
import org.chiu.micro.blog.repository.BlogRepository;
import org.chiu.micro.blog.req.BlogEditPushAllReq;
import org.chiu.micro.blog.service.BlogEditService;
import org.chiu.micro.blog.service.BlogSensitiveService;
import org.chiu.micro.blog.utils.AuthUtils;
import org.chiu.micro.blog.utils.JsonUtils;
import org.chiu.micro.blog.vo.BlogEditVo;
import org.chiu.micro.blog.vo.BlogSensitiveContentVo;
import org.chiu.micro.blog.vo.SensitiveContentVo;
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

import static org.chiu.micro.blog.lang.Const.*;
import static org.chiu.micro.blog.lang.ExceptionMessage.NO_FOUND;
import static org.chiu.micro.blog.lang.MessageActionFieldEnum.*;

@Service
public class BlogEditServiceImpl implements BlogEditService {

    private final StringRedisTemplate redisTemplate;

    private String pushAllScript;

    private final ResourceLoader resourceLoader;

    private final JsonUtils jsonUtils;

    private final BlogSensitiveService blogSensitiveService;

    private final BlogRepository blogRepository;

    private final TypeReference<List<SensitiveContentVo>> type = new TypeReference<>() {
    };

    public BlogEditServiceImpl(StringRedisTemplate redisTemplate, ResourceLoader resourceLoader, JsonUtils jsonUtils, BlogSensitiveService blogSensitiveService, BlogRepository blogRepository) {
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
        this.jsonUtils = jsonUtils;
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

        String redisKey = KeyFactory.createBlogEditRedisKey(originUserId, id.orElse(null));
        boolean exist = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        if (!exist) {
            return;
        }

        String content = blog.content();

        String[] splits = content.split(PARAGRAPH_SPLITTER.getInfo());
        List<String> paragraphList = new ArrayList<>(splits.length + 2);
        Collections.addAll(paragraphList, splits);
        if (content.endsWith(PARAGRAPH_SPLITTER.getInfo())) {
            paragraphList.add("");
        }
        String paragraphListString = jsonUtils.writeValueAsString(paragraphList);

        redisTemplate.execute(RedisScript.of(pushAllScript),
                Collections.singletonList(redisKey),
                paragraphListString, ID.getMsg(), USER_ID.getMsg(), TITLE.getMsg(), DESCRIPTION.getMsg(),
                STATUS.getMsg(), LINK.getMsg(), VERSION.getMsg(), SENSITIVE_CONTENT_LIST.getMsg(),
                Objects.isNull(blog.id()) ? "" : blog.id().toString(), originUserId.toString(), blog.title(),
                blog.description(), blog.status().toString(), blog.link(), blog.version().toString(), jsonUtils.writeValueAsString(blog.sensitiveContentList()),
                A_WEEK.getInfo());
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

        String redisKey = KeyFactory.createBlogEditRedisKey(originUserId, id);
        Map<String, String> entries = redisTemplate.<String, String>opsForHash()
                .entries(redisKey);

        BlogEntityDto blog;
        List<SensitiveContentVo> sensitiveContentList;
        int version = -1;
        String paragraphListString = null;
        if (!entries.isEmpty()) {
            blog = BlogEntityConvertor.convert(entries);
            sensitiveContentList = jsonUtils.readValue(entries.get(SENSITIVE_CONTENT_LIST.getMsg()), type);
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
                String idx = PARAGRAPH_PREFIX.getInfo() + i;
                content.append(entries.get(idx));
                if (i != entries.size()) {
                    content.append(PARAGRAPH_SPLITTER.getInfo());
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
            List<String> paragraphList = List.of(blog.content().split(PARAGRAPH_SPLITTER.getInfo()));
            paragraphListString = jsonUtils.writeValueAsString(paragraphList);
            BlogSensitiveContentVo blogSensitiveContentVo = blogSensitiveService.findByBlogId(id);
            sensitiveContentList = blogSensitiveContentVo.sensitiveContent();
        }

        if (StringUtils.hasLength(paragraphListString)) {
            redisTemplate.execute(RedisScript.of(pushAllScript),
                    Collections.singletonList(redisKey),
                    paragraphListString, ID.getMsg(), USER_ID.getMsg(), TITLE.getMsg(), DESCRIPTION.getMsg(),
                    STATUS.getMsg(), LINK.getMsg(), VERSION.getMsg(), SENSITIVE_CONTENT_LIST.getMsg(),
                    Objects.isNull(blog.id()) ? "" : blog.id().toString(), originUserId.toString(), blog.title(),
                    blog.description(), blog.status().toString(), blog.link(), Integer.toString(version), jsonUtils.writeValueAsString(sensitiveContentList),
                    A_WEEK.getInfo());
        }

        return BlogEditVoConvertor.convert(blog, version, sensitiveContentList);
    }
}
