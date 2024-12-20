package wiki.chiu.micro.user.service.impl;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.utils.OssSignUtils;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.convertor.UserEntityRpcVoConvertor;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.service.UserService;
import wiki.chiu.micro.user.vo.UserEntityRpcVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static wiki.chiu.micro.common.lang.Const.REGISTER_PREFIX;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String IMAGE_JPG = "image/jpg";

    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate;

    private final OssHttpService ossHttpService;

    private final ExecutorService taskExecutor;

    @Value("${megalith.blog.oss.base-url}")
    private String baseUrl;

    @Value("${megalith.blog.register.page-prefix}")
    private String pagePrefix;

    @Value("${megalith.blog.aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${megalith.blog.aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${megalith.blog.aliyun.oss.bucket-name}")
    private String bucketName;

    public UserServiceImpl(UserRepository userRepository, StringRedisTemplate redisTemplate, OssHttpService ossHttpService, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.ossHttpService = ossHttpService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void updateLoginTime(String username, LocalDateTime time) {
        userRepository.updateLoginTime(username, time);
    }

    @Override
    public void changeUserStatusByUsername(String username, Integer status) {
        userRepository.updateUserStatusByUsername(username, status);
    }

    @Override
    public String getRegisterPage(String username) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(REGISTER_PREFIX + token, username, 1, TimeUnit.HOURS);
        return StringUtils.hasLength(username) ? pagePrefix + token + "?username=" + username : pagePrefix + token;
    }

    @Override
    public SseEmitter imageUpload(String token, MultipartFile file) {
        validateToken(token);
        byte[] imageBytes = getImageBytes(file);
        SseEmitter sseEmitter = new SseEmitter();

        taskExecutor.execute(() -> {
            String objectName = getObjectName(file);
            Map<String, String> headers = getHeaders(objectName, HttpMethod.PUT.name(), IMAGE_JPG);
            ossHttpService.putOssObject(objectName, imageBytes, headers);
            sendSseEmitter(sseEmitter, baseUrl + "/" + objectName);
        });

        return sseEmitter;
    }

    @Override
    public void imageDelete(String token, String url) {
        validateToken(token);
        taskExecutor.execute(() -> {
            String objectName = url.replace(baseUrl + "/", "");
            Map<String, String> headers = getHeaders(objectName, HttpMethod.DELETE.name(), "");
            ossHttpService.deleteOssObject(objectName, headers);
        });
    }

    @Override
    public Boolean checkRegisterPage(String token) {
        return redisTemplate.hasKey(REGISTER_PREFIX + token);
    }

    @Override
    public void download(HttpServletResponse response) {
        List<UserEntity> userEntities = userRepository.findAll();
        String users = SQLUtils.entityToInsertSQL(userEntities, Const.USER_TABLE);
        writeResponse(response, users);
    }

    @Override
    public UserEntityRpcVo findById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new MissException(USER_MISS.getMsg()));
        return UserEntityRpcVoConvertor.convert(user);
    }

    @Override
    public UserEntityRpcVo findByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new MissException(EMAIL_NOT_EXIST.getMsg()));
        return UserEntityRpcVoConvertor.convert(userEntity);
    }

    @Override
    public UserEntityRpcVo findByPhone(String phone) {
        UserEntity userEntity = userRepository.findByPhone(phone)
                .orElseThrow(() -> new MissException(PHONE_NOT_EXIST.getMsg()));
        return UserEntityRpcVoConvertor.convert(userEntity);
    }

    @Override
    public UserEntityRpcVo findByUsernameOrEmailOrPhone(String username) {
        UserEntity userEntity = userRepository.findByUsernameOrEmailOrPhone(username, username, username)
                .orElseThrow(() -> new MissException(USER_MISS.getMsg()));
        return UserEntityRpcVoConvertor.convert(userEntity);
    }

    @Override
    public void unlockUser() {
        userRepository.unlockUser();
    }

    private void validateToken(String token) {
        Boolean exist = redisTemplate.hasKey(REGISTER_PREFIX + token);
        if (Boolean.FALSE.equals(exist)) {
            throw new MissException(NO_AUTH.getMsg());
        }
    }

    private byte[] getImageBytes(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            if (imageBytes.length == 0) {
                throw new MissException(UPLOAD_MISS.getMsg());
            }
            return imageBytes;
        } catch (IOException e) {
            throw new MissException(e.getMessage());
        }
    }

    private String getObjectName(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElseGet(() -> UUID.randomUUID().toString())
                .replace(" ", "");
        return "avatar/" + uuid + "-" + originalFilename;
    }

    private Map<String, String> getHeaders(String objectName, String method, String contentType) {
        Map<String, String> headers = new HashMap<>();
        String gmtDate = OssSignUtils.getGMTDate();
        headers.put(HttpHeaders.DATE, gmtDate);
        headers.put(HttpHeaders.AUTHORIZATION, OssSignUtils.getAuthorization(objectName, method, contentType, accessKeyId, accessKeySecret, bucketName));
        headers.put(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        return headers;
    }

    private void sendSseEmitter(SseEmitter sseEmitter, String url) {
        try {
            sseEmitter.send(url, MediaType.TEXT_PLAIN);
            sseEmitter.complete();
        } catch (IOException e) {
            sseEmitter.completeWithError(e);
        }
    }

    private void writeResponse(HttpServletResponse response, String data) {
        try {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new MissException(e.getMessage());
        }
    }
}
