package wiki.chiu.micro.websocket.config.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import wiki.chiu.micro.common.utils.JsonUtils;
import wiki.chiu.micro.common.utils.KeyUtils;
import wiki.chiu.micro.websocket.dto.BlogEditPushActionDto;
import wiki.chiu.micro.websocket.dto.MessageDto;
import wiki.chiu.micro.websocket.lang.MessageEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class EditWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(EditWebSocketHandler.class);

    private final StringRedisTemplate redisTemplate;

    private final ResourceLoader resourceLoader;

    private final ObjectMapper objectMapper;

    private String pushActionScript;

    private final Set<Long> enumSet = Stream.of(MessageEnum.values())
            .map(MessageEnum::getCode)
            .collect(Collectors.toSet());

    public EditWebSocketHandler(StringRedisTemplate redisTemplate, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() throws IOException {
        Resource pushActionResource = resourceLoader.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "script/push-action.lua");
        pushActionScript = pushActionResource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        Principal principal = session.getPrincipal();

        if (principal == null) {
            return;
        }

        Long userId = Long.valueOf(principal.getName());
        String payload = message.getPayload();
        BlogEditPushActionDto pushActionDto = JsonUtils.readValue(objectMapper, payload, BlogEditPushActionDto.class);

        Long blogId = pushActionDto.id();
        String contentChange = pushActionDto.contentChange();
        Integer operateTypeCode = pushActionDto.operateTypeCode();
        Integer version = pushActionDto.version();
        Integer indexStart = pushActionDto.indexStart();
        Integer indexEnd = pushActionDto.indexEnd();
        String field = pushActionDto.field();
        Integer paraNo = pushActionDto.paraNo();

        String redisKey = KeyUtils.createBlogEditRedisKey(userId, blogId);

        Long execute = redisTemplate.execute(RedisScript.of(pushActionScript, Long.class), Collections.singletonList(redisKey),
                contentChange,
                operateTypeCode.toString(),
                version.toString(),
                Objects.nonNull(indexStart) ? indexStart.toString() : null,
                Objects.nonNull(indexEnd) ? indexEnd.toString() : null,
                Objects.nonNull(field) ? field : null,
                Objects.nonNull(paraNo) ? paraNo.toString() : null,
                userId.toString());

        if (enumSet.contains(execute)) {
            var dto = MessageDto.builder()
                    .blogId(blogId)
                    .userId(userId)
                    .version(version)
                    .type(execute.intValue())
                    .build();

            TextMessage textMessage = new TextMessage(JsonUtils.writeValueAsString(objectMapper, dto));
            session.sendMessage(textMessage);
        }
    }


}
