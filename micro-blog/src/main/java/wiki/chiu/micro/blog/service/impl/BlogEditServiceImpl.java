package wiki.chiu.micro.blog.service.impl;

import org.springframework.beans.factory.annotation.Value;
import wiki.chiu.micro.blog.convertor.BlogEditVoConvertor;
import wiki.chiu.micro.blog.convertor.SensitiveContentVoConvertor;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.service.BlogEditService;
import wiki.chiu.micro.blog.utils.EditAuthUtils;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

import org.springframework.stereotype.Service;

import java.util.*;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

@Service
public class BlogEditServiceImpl implements BlogEditService {

    private final BlogSensitiveContentRepository blogSensitiveContentRepository;

    private final BlogRepository blogRepository;


    @Value("${megalith.blog.highest-role}")
    private String highestRole;

    public BlogEditServiceImpl(BlogSensitiveContentRepository blogSensitiveContentRepository, BlogRepository blogRepository) {
        this.blogSensitiveContentRepository = blogSensitiveContentRepository;
        this.blogRepository = blogRepository;
    }

    @Override
    public BlogEditVo findEdit(Long id, Long userId, List<String> roles) {

        BlogEntity blog;
        List<BlogEditVo.SensitiveContentVo> sensitiveContentList;
        Long blogUserId;
        if (id != null) {
            blog = blogRepository.findById(id)
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
            EditAuthUtils.checkEditAuth(blog, userId);
            blogUserId = blog.getUserId();
            var sensitiveContentRpcList = blogSensitiveContentRepository.findByBlogId(id);
            sensitiveContentList = SensitiveContentVoConvertor.convert(sensitiveContentRpcList);
        } else {
            blog = createNewBlog();
            sensitiveContentList = new ArrayList<>();
            blogUserId = userId;
        }

        return BlogEditVoConvertor.convert(blog, sensitiveContentList, userId, blogUserId, roles, highestRole);
    }

    private BlogEntity createNewBlog() {
        return BlogEntity.builder()
                .status(BlogStatusEnum.NORMAL.getCode())
                .content("")
                .description("")
                .link("")
                .title("")
                .build();
    }
}
