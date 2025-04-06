package wiki.chiu.micro.blog.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import wiki.chiu.micro.blog.convertor.BlogEditVoConvertor;
import wiki.chiu.micro.blog.convertor.BlogEntityDtoConvertor;
import wiki.chiu.micro.blog.convertor.SensitiveContentVoConvertor;
import wiki.chiu.micro.blog.dto.BlogEntityDto;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.service.BlogEditService;
import wiki.chiu.micro.blog.service.BlogSensitiveService;
import wiki.chiu.micro.blog.utils.EditAuthUtils;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.vo.SensitiveContentRpcVo;

import java.util.*;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

@Service
public class BlogEditServiceImpl implements BlogEditService {

    private final BlogSensitiveService blogSensitiveService;

    private final BlogRepository blogRepository;


    private final TypeReference<List<SensitiveContentRpcVo>> type = new TypeReference<>() {
    };

    public BlogEditServiceImpl(BlogSensitiveService blogSensitiveService, BlogRepository blogRepository) {
        this.blogSensitiveService = blogSensitiveService;
        this.blogRepository = blogRepository;
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


        BlogEntityDto blog;
        List<BlogEditVo.SensitiveContentVo> sensitiveContentList;

        if (Objects.isNull(id)) {
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
