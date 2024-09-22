package org.chiu.micro.websocket.service.impl;

import jakarta.annotation.PostConstruct;
import org.chiu.micro.websocket.dto.StompMessageDto;
import org.chiu.micro.websocket.key.KeyFactory;
import org.chiu.micro.websocket.lang.MessageEnum;
import org.chiu.micro.websocket.req.BlogEditPushActionReq;
import org.chiu.micro.websocket.service.BlogMessageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BlogMessageServiceImpl implements BlogMessageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final StringRedisTemplate redisTemplate;

    private final ResourceLoader resourceLoader;

    private String pushActionScript;

    private final Set<Long> enumSet = Stream.of(MessageEnum.values())
            .map(MessageEnum::getCode)
            .collect(Collectors.toSet());

    public BlogMessageServiceImpl(SimpMessagingTemplate simpMessagingTemplate, StringRedisTemplate redisTemplate, ResourceLoader resourceLoader) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource pushActionResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/push-action.lua");
        pushActionScript = pushActionResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public void pushAction(BlogEditPushActionReq req, Long userId) {
        Long blogId = req.id();
        String contentChange = req.contentChange();
        Integer operateTypeCode = req.operateTypeCode();
        Integer version = req.version();
        Integer indexStart = req.indexStart();
        Integer indexEnd = req.indexEnd();
        String field = req.field();
        Integer paraNo = req.paraNo();

        String redisKey = KeyFactory.createBlogEditRedisKey(userId, blogId);

        Long execute = redisTemplate.execute(RedisScript.of(pushActionScript, Long.class), Collections.singletonList(redisKey),
                contentChange,
                operateTypeCode.toString(),
                version.toString(),
                Objects.nonNull(indexStart) ? indexStart.toString() : null,
                Objects.nonNull(indexEnd) ? indexEnd.toString() : null,
                Objects.nonNull(field) ? field : null,
                Objects.nonNull(paraNo) ? paraNo.toString() : null,
                userId.toString());

        if (execute != null && enumSet.contains(execute)) {
            var dto = StompMessageDto.builder()
                    .blogId(blogId)
                    .userId(userId)
                    .version(version)
                    .type(execute.intValue())
                    .build();

            String subscriptionKey = KeyFactory.createSubscriptionKey(userId, blogId);
            simpMessagingTemplate.convertAndSend("/edits" + subscriptionKey, dto);
        }
    }

}
