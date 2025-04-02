package wiki.chiu.micro.blog.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import wiki.chiu.micro.blog.convertor.BlogEditVoConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityDtoConvertor;
import wiki.chiu.micro.blog.convertor.BlogPushAllDtoConvertor;
import wiki.chiu.micro.blog.convertor.SensitiveContentVoConvertor;
import wiki.chiu.micro.blog.dto.BlogEntityDto;
import wiki.chiu.micro.blog.dto.BlogPushAllDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.req.BlogEditPushAllReq;
import wiki.chiu.micro.blog.service.BlogEditService;
import wiki.chiu.micro.blog.service.BlogSensitiveService;
import wiki.chiu.micro.blog.utils.EditAuthUtils;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;
import wiki.chiu.micro.common.utils.JsonUtils;
import wiki.chiu.micro.common.utils.KeyUtils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.vo.SensitiveContentRpcVo;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

@Service
public class BlogEditServiceImpl implements BlogEditService {

    private final StringRedisTemplate redisTemplate;

    private final BlogSensitiveService blogSensitiveService;

    private final BlogRepository blogRepository;

    private final ObjectMapper objectMapper;

    private final TypeReference<List<SensitiveContentRpcVo>> type = new TypeReference<>() {
    };

    public BlogEditServiceImpl(StringRedisTemplate redisTemplate, BlogSensitiveService blogSensitiveService, BlogRepository blogRepository, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.blogSensitiveService = blogSensitiveService;
        this.blogRepository = blogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void pushAll(BlogEditPushAllReq blog, Long userId) {
        Long originUserId;
        if (blog.id().isPresent()) {
            BlogEntity blogEntity = blogRepository.findById(blog.id().get()).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
            EditAuthUtils.checkEditAuth(blogEntity, userId);
            originUserId = blogEntity.getUserId();
        } else {
            originUserId = userId;
        }
        String redisKey = KeyUtils.createBlogEditRedisKey(originUserId, blog.id().orElse(null));
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            BlogPushAllDto dto = BlogPushAllDtoConvertor.convert(blog);
            redisTemplate.opsForValue().set(redisKey, JsonUtils.writeValueAsString(objectMapper, dto), 7, TimeUnit.DAYS);
        }
    }

    @Override
    public BlogEditVo findEdit(Long id, Long userId) {
        Long originUserId;
        if (id != null) {
            BlogEntity blogEntity = blogRepository.findById(id)
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
            EditAuthUtils.checkEditAuth(blogEntity, userId);
            originUserId = blogEntity.getUserId();
        } else {
            originUserId = userId;
        }

        String redisKey = KeyUtils.createBlogEditRedisKey(originUserId, id);
        String cacheBlogStr = redisTemplate.opsForValue().get(redisKey);

        BlogEntityDto blog;
        List<BlogEditVo.SensitiveContentVo> sensitiveContentList;

        if (StringUtils.hasLength(cacheBlogStr)) {
            BlogPushAllDto dto = JsonUtils.readValue(objectMapper, cacheBlogStr, BlogPushAllDto.class);
            blog = BlogEntityDtoConvertor.convert(dto);
            sensitiveContentList = SensitiveContentVoConvertor.convert(dto);
        } else if (Objects.isNull(id)) {
            blog = createNewBlog(originUserId);
            sensitiveContentList = new ArrayList<>();
        } else {
            blog = getExistingBlog(id);
            var sensitiveContentRpcList = blogSensitiveService.findByBlogId(id).sensitiveContent();
            sensitiveContentList = SensitiveContentVoConvertor.convert(sensitiveContentRpcList);
        }
        return BlogEditVoConvertor.convert(blog, sensitiveContentList);
    }

    private BlogEntityDto createNewBlog(Long originUserId) {
        return BlogEntityDto.builder()
                .status(BlogStatusEnum.NORMAL.getCode())
                .userId(originUserId)
                .content("")
                .description("")
                .link("")
                .title("")
                .build();
    }

    private BlogEntityDto getExistingBlog(Long id) {
        BlogEntity userBlogEntity = blogRepository.findById(id)
                .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        return BlogEntityDtoConvertor.convert(userBlogEntity);
    }
}
