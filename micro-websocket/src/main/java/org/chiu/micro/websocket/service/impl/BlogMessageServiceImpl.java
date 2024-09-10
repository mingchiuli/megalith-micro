package org.chiu.micro.websocket.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

import org.chiu.micro.websocket.dto.BlogEntityDto;
import org.chiu.micro.websocket.dto.StompMessageDto;
import org.chiu.micro.websocket.key.KeyFactory;
import org.chiu.micro.websocket.lang.MessageEnum;
import org.chiu.micro.websocket.req.BlogEditPushActionReq;
import org.chiu.micro.websocket.rpc.wrapper.BlogHttpServiceWrapper;
import org.chiu.micro.websocket.service.BlogMessageService;
import org.chiu.micro.websocket.utils.AuthUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BlogMessageServiceImpl implements BlogMessageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final StringRedisTemplate redisTemplate;

    private final ResourceLoader resourceLoader;

    private String pushActionScript;

    private Set<Long> enumSet = Stream.of(MessageEnum.values())
                .map(MessageEnum::getCode)
                .collect(Collectors.toSet());

    @PostConstruct
    @SneakyThrows
    private void init() {
        Resource pushActionResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/push-action.lua");
        pushActionScript = pushActionResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public void pushAction(BlogEditPushActionReq req, Long userId) {
        Long blogId = req.getId();
        if (blogId != null) {
            BlogEntityDto blogEntityDto = blogHttpServiceWrapper.findById(blogId);
            AuthUtils.checkEditAuth(blogEntityDto, userId);
        }
        
        String contentChange = req.getContentChange();
        Integer operateTypeCode = req.getOperateTypeCode();
        Integer version = req.getVersion();
        Integer indexStart = req.getIndexStart();
        Integer indexEnd = req.getIndexEnd();
        String field = req.getField();
        Integer paraNo = req.getParaNo();

        String redisKey = KeyFactory.createBlogEditRedisKey(userId, blogId);

        Long execute = redisTemplate.execute(RedisScript.of(pushActionScript, Long.class), Collections.singletonList(redisKey),
                contentChange,
                operateTypeCode.toString(),
                version.toString(),
                Objects.nonNull(indexStart) ? indexStart.toString() : null,
                Objects.nonNull(indexEnd) ? indexEnd.toString() : null,
                Objects.nonNull(field) ? field : null,
                Objects.nonNull(paraNo) ? paraNo.toString() : null);

        if (enumSet.contains(execute)) {
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
