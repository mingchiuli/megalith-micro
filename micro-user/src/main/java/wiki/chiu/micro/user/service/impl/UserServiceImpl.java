package wiki.chiu.micro.user.service.impl;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.convertor.UserEntityRpcVoConvertor;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.service.UserService;
import wiki.chiu.micro.user.utils.OssSignUtils;
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

    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate;

    private final OssHttpService ossHttpService;

    private final OssSignUtils ossSignUtils;


    private final ExecutorService taskExecutor;

    @Value("${megalith.blog.oss.base-url}")
    private String baseUrl;

    @Value("${megalith.blog.register.page-prefix}")
    private String pagePrefix;

    public UserServiceImpl(UserRepository userRepository, StringRedisTemplate redisTemplate, OssHttpService ossHttpService, OssSignUtils ossSignUtils, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.ossHttpService = ossHttpService;
        this.ossSignUtils = ossSignUtils;
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
        if (StringUtils.hasLength(username)) {
            return pagePrefix + token + "?username=" + username;
        }
        return pagePrefix + token;
    }

    @Override
    public SseEmitter imageUpload(String token, MultipartFile file) {
        Boolean exist = redisTemplate.hasKey(REGISTER_PREFIX + token);
        if (Boolean.FALSE.equals(exist)) {
            throw new MissException(NO_AUTH.getMsg());
        }
        byte[] imageBytes;
        try {
            imageBytes = file.getBytes();
        } catch (IOException e) {
            throw new MissException(e.getMessage());
        }
        if (imageBytes.length == 0) {
            throw new MissException(UPLOAD_MISS.getMsg());
        }
        var sseEmitter = new SseEmitter();

        taskExecutor.execute(() -> {
            String uuid = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            originalFilename = Optional.ofNullable(originalFilename)
                    .orElseGet(() -> UUID.randomUUID().toString())
                    .replace(" ", "");
            String objectName = "avatar/" + uuid + "-" + originalFilename;

            Map<String, String> headers = new HashMap<>();
            String gmtDate = ossSignUtils.getGMTDate();
            headers.put(HttpHeaders.DATE, gmtDate);
            headers.put(HttpHeaders.AUTHORIZATION, ossSignUtils.getAuthorization(objectName, HttpMethod.PUT.name(), "image/jpg"));
            headers.put(HttpHeaders.CACHE_CONTROL, "no-cache");
            headers.put(HttpHeaders.CONTENT_TYPE, "image/jpg");
            ossHttpService.putOssObject(objectName, imageBytes, headers);
            try {
                sseEmitter.send(baseUrl + "/" + objectName, MediaType.TEXT_PLAIN);
                sseEmitter.complete();
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        });

        return sseEmitter;
    }

    @Override
    public void imageDelete(String token, String url) {
        Boolean exist = redisTemplate.hasKey(REGISTER_PREFIX + token);
        if (Boolean.FALSE.equals(exist)) {
            throw new MissException(NO_AUTH.getMsg());
        }
        taskExecutor.execute(() -> {
            String objectName = url.replace(baseUrl + "/", "");
            Map<String, String> headers = new HashMap<>();
            String gmtDate = ossSignUtils.getGMTDate();
            headers.put(HttpHeaders.DATE, gmtDate);
            headers.put(HttpHeaders.AUTHORIZATION, ossSignUtils.getAuthorization(objectName, HttpMethod.DELETE.name(), ""));
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
        try {
            byte[] data = users.getBytes();
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new MissException(e.getMessage());
        }
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
}
