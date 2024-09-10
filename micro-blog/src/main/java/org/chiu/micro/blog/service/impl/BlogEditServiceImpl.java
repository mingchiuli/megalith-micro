package org.chiu.micro.blog.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import org.chiu.micro.blog.vo.SensitiveContentVo;
import org.chiu.micro.blog.vo.BlogEditVo;
import org.chiu.micro.blog.vo.BlogSensitiveContentVo;
import org.chiu.micro.blog.service.BlogEditService;
import org.chiu.micro.blog.service.BlogSensitiveService;
import org.chiu.micro.blog.utils.AuthUtils;
import org.chiu.micro.blog.utils.JsonUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import static org.chiu.micro.blog.lang.MessageActionFieldEnum.*;
import static org.chiu.micro.blog.lang.Const.*;
import static org.chiu.micro.blog.lang.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class BlogEditServiceImpl implements BlogEditService {

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate redisTemplate;

    private String pushAllScript;

    private final ResourceLoader resourceLoader;

    private final JsonUtils jsonUtils;

    private final BlogSensitiveService blogSensitiveService;

    private final BlogRepository blogRepository;

    private final TypeReference<List<SensitiveContentVo>> type = new TypeReference<>() {};

    @PostConstruct
    @SneakyThrows
    private void init() {
        Resource pushAllResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/push-all.lua");
        pushAllScript = pushAllResource.getContentAsString(StandardCharsets.UTF_8);
    }
  
    @Override
    public void pushAll(BlogEditPushAllReq blog, Long userId) {
        Long id = blog.getId();

        BlogEntity blogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        AuthUtils.checkEditAuth(blogEntity, userId);

        String redisKey = Objects.isNull(id) ?
                TEMP_EDIT_BLOG.getInfo() + userId :
                TEMP_EDIT_BLOG.getInfo() + userId + ":" + id;
        boolean exist = redisTemplate.hasKey(redisKey);
        if (!exist) {
            return;
        }

        String content = blog.getContent();

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
                Objects.isNull(blog.getId()) ? "" : blog.getId().toString(), userId.toString(), blog.getTitle(),
                blog.getDescription(), blog.getStatus().toString(), blog.getLink(), blog.getVersion().toString(), jsonUtils.writeValueAsString(blog.getSensitiveContentList()),
                A_WEEK.getInfo());
    }

    @Override
    @SneakyThrows
    public BlogEditVo findEdit(Long id, Long userId) {

        BlogEntity blogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        AuthUtils.checkEditAuth(blogEntity, userId);

        String redisKey = KeyFactory.createBlogEditRedisKey(userId, id);
        Map<String, String> entries = redisTemplate.<String, String>opsForHash()
                .entries(redisKey);

        BlogEntityDto blog;
        List<SensitiveContentVo> sensitiveContentList;
        int version = -1;
        String paragraphListString = null;
        if (!entries.isEmpty()) {
            blog = BlogEntityConvertor.convert(entries);
            sensitiveContentList = objectMapper.readValue(entries.get(SENSITIVE_CONTENT_LIST.getMsg()), type);
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
            blog.setContent(content.toString());
        } else if (Objects.isNull(id)) {
            // 新文章
            blog = BlogEntityDto.builder()
                    .status(StatusEnum.NORMAL.getCode())
                    .userId(userId)
                    .content("")
                    .description("")
                    .link("")
                    .title("")
                    .build();
            paragraphListString = "[]";
            sensitiveContentList = new ArrayList<>();
        } else {
            BlogEntity userBlogEntity = blogRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
            
            blog = BlogEntityDtoConvertor.convert(userBlogEntity);
            List<String> paragraphList = List.of(blog.getContent().split(PARAGRAPH_SPLITTER.getInfo()));
            paragraphListString = jsonUtils.writeValueAsString(paragraphList);
            BlogSensitiveContentVo blogSensitiveContentVo = blogSensitiveService.findByBlogId(id);
            sensitiveContentList = blogSensitiveContentVo.getSensitiveContent();
        }

        if (StringUtils.hasLength(paragraphListString)) {
            redisTemplate.execute(RedisScript.of(pushAllScript),
                    Collections.singletonList(redisKey),
                    paragraphListString, ID.getMsg(), USER_ID.getMsg(), TITLE.getMsg(), DESCRIPTION.getMsg(),
                            STATUS.getMsg(), LINK.getMsg(), VERSION.getMsg(), SENSITIVE_CONTENT_LIST.getMsg(),
                    Objects.isNull(blog.getId()) ? "" : blog.getId().toString(), userId.toString(), blog.getTitle(),
                            blog.getDescription(), blog.getStatus().toString(), blog.getLink(), Integer.toString(version), jsonUtils.writeValueAsString(sensitiveContentList),
                    A_WEEK.getInfo());
        }

        return BlogEditVoConvertor.convert(blog, version, sensitiveContentList);
    }
}
