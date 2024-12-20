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
import java.util.Optional;
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
        Optional.ofNullable(session.getPrincipal())
                .ifPresent(principal -> processMessage(session, message, principal));
    }

    private void processMessage(WebSocketSession session, TextMessage message, Principal principal) {
        try {
            Long userId = Long.valueOf(principal.getName());
            BlogEditPushActionDto pushActionDto = JsonUtils.readValue(objectMapper, message.getPayload(), BlogEditPushActionDto.class);

            String redisKey = KeyUtils.createBlogEditRedisKey(userId, pushActionDto.id());
            Long execute = redisTemplate.execute(RedisScript.of(pushActionScript, Long.class), Collections.singletonList(redisKey),
                    pushActionDto.contentChange(),
                    pushActionDto.operateTypeCode().toString(),
                    pushActionDto.version().toString(),
                    Optional.ofNullable(pushActionDto.indexStart())
                            .map(Object::toString)
                            .orElse(null),
                    Optional.ofNullable(pushActionDto.indexEnd())
                            .map(Object::toString)
                            .orElse(null),
                    pushActionDto.field(),
                    Optional.ofNullable(pushActionDto.paraNo())
                            .map(Object::toString)
                            .orElse(null),
                    userId.toString());

            if (enumSet.contains(execute)) {
                sendMessage(session, pushActionDto.id(), userId, pushActionDto.version(), execute.intValue());
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
        }
    }

    private void sendMessage(WebSocketSession session, Long blogId, Long userId, Integer version, int type) throws IOException {
        MessageDto dto = MessageDto.builder()
                .blogId(blogId)
                .userId(userId)
                .version(version)
                .type(type)
                .build();

        TextMessage textMessage = new TextMessage(JsonUtils.writeValueAsString(objectMapper, dto));
        session.sendMessage(textMessage);
    }


}
