package org.chiu.micro.blog.listener;

import jakarta.annotation.PostConstruct;
import org.chiu.micro.blog.config.BlogChangeRabbitConfig;
import org.chiu.micro.blog.constant.BlogOperateEnum;
import org.chiu.micro.blog.constant.BlogOperateMessage;
import org.chiu.micro.blog.entity.BlogEntity;
import org.chiu.micro.blog.event.BlogOperateEvent;
import org.chiu.micro.blog.repository.BlogRepository;
import org.chiu.micro.common.exception.MissException;
import org.chiu.micro.common.utils.JsonUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.chiu.micro.common.lang.Const.A_WEEK;
import static org.chiu.micro.common.lang.Const.QUERY_DELETED;
import static org.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;


@Component
public class BlogOperateEventListener {

    private final RabbitTemplate rabbitTemplate;

    public final StringRedisTemplate redisTemplate;

    private final ResourceLoader resourceLoader;

    private final JsonUtils jsonUtils;

    private final BlogRepository blogRepository;

    private String blogDeleteScript;

    public BlogOperateEventListener(RabbitTemplate rabbitTemplate, StringRedisTemplate redisTemplate, ResourceLoader resourceLoader, JsonUtils jsonUtils, BlogRepository blogRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
        this.jsonUtils = jsonUtils;
        this.blogRepository = blogRepository;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource blogDeleteResource = resourceLoader
                .getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/blog-delete.lua");
        blogDeleteScript = blogDeleteResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @EventListener
    @Async("commonExecutor")
    public void process(BlogOperateEvent event) {
        BlogOperateMessage messageBody = event.getBlogOperateMessage();

        BlogOperateEnum blogOperateEnum = messageBody.typeEnum();

        if (BlogOperateEnum.REMOVE.equals(blogOperateEnum)) {
            Long blogId = messageBody.blogId();
            BlogEntity blogEntity = blogRepository.findById(blogId)
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));

            redisTemplate.execute(RedisScript.of(blogDeleteScript),
                    Collections.singletonList(QUERY_DELETED + messageBody.blogId()),
                    jsonUtils.writeValueAsString(blogEntity), A_WEEK);
        }

        rabbitTemplate.convertAndSend(BlogChangeRabbitConfig.FANOUT_EXCHANGE, "", messageBody);
    }
}
