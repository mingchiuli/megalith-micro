package org.chiu.micro.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.chiu.micro.user.http.OssHttpService;
import org.chiu.micro.user.utils.OssSignUtils;
import org.chiu.micro.user.vo.UserEntityRpcVo;
import org.chiu.micro.user.convertor.UserEntityRpcVoConvertor;
import org.chiu.micro.user.entity.UserEntity;
import org.chiu.micro.user.exception.MissException;
import org.chiu.micro.user.repository.UserRepository;
import org.chiu.micro.user.req.ImgUploadReq;
import org.chiu.micro.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.chiu.micro.user.lang.Const.*;
import static org.chiu.micro.user.lang.ExceptionMessage.*;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate;

    private final OssHttpService ossHttpService;

    private final OssSignUtils ossSignUtils;

    private final ObjectMapper objectMapper;

    @Value("${blog.oss.base-url}")
    private String baseUrl;

    @Value("${blog.register.page-prefix}")
    private String pagePrefix;

    @Override
    public void updateLoginTime(String username, LocalDateTime time) {
        userRepository.updateLoginTime(username, time);
    }

    @Override
    public void changeUserStatusByUsername(String username, Integer status) {
        userRepository.updateUserStatusByUsername(username, status);
    }

    @Override
    public List<Long> findIdsByStatus(Integer status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public String getRegisterPage(String username) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(REGISTER_PREFIX.getInfo() + token, username, 1, TimeUnit.HOURS);
        if (StringUtils.hasLength(username)) {
            return pagePrefix + token + "?username=" + username;
        }
        return pagePrefix + token;
    }

    @SneakyThrows
    @Override
    public String imageUpload(String token, ImgUploadReq req) {
        Boolean exist = redisTemplate.hasKey(REGISTER_PREFIX.getInfo() + token);
        if (Objects.isNull(exist) || Boolean.FALSE.equals(exist)) {
            throw new MissException(NO_AUTH.getMsg());
        }

        Assert.notNull(req.getData(), UPLOAD_MISS.getMsg());
        String uuid = UUID.randomUUID().toString();
        String originalFilename = req.getFileName();
        originalFilename = Optional.ofNullable(originalFilename)
                .orElseGet(() -> UUID.randomUUID().toString())
                .replace(" ", "");
        String objectName = "avatar/" + uuid + "-" + originalFilename;
        byte[] imageBytes = req.getData();

        Map<String, String> headers = new HashMap<>();
        String gmtDate = ossSignUtils.getGMTDate();
        headers.put(HttpHeaders.DATE, gmtDate);
        headers.put(HttpHeaders.AUTHORIZATION, ossSignUtils.getAuthorization(objectName, HttpMethod.PUT.name(), "image/jpg"));
        headers.put(HttpHeaders.CACHE_CONTROL,  "no-cache");
        headers.put(HttpHeaders.CONTENT_TYPE, "image/jpg");
        ossHttpService.putOssObject(objectName, imageBytes, headers);
        return baseUrl + "/" + objectName;
    }

    @Override
    public void imageDelete(String token, String url) {
        Boolean exist = redisTemplate.hasKey(REGISTER_PREFIX.getInfo() + token);
        if (Objects.isNull(exist) || Boolean.FALSE.equals(exist)) {
            throw new MissException(NO_AUTH.getMsg());
        }
        String objectName = url.replace(baseUrl + "/", "");
        Map<String, String> headers = new HashMap<>();
        String gmtDate = ossSignUtils.getGMTDate();
        headers.put(HttpHeaders.DATE, gmtDate);
        headers.put(HttpHeaders.AUTHORIZATION, ossSignUtils.getAuthorization(objectName, HttpMethod.DELETE.name(), ""));
        ossHttpService.deleteOssObject(objectName, headers);
    }

    @Override
    public Boolean checkRegisterPage(String token) {
        return redisTemplate.hasKey(REGISTER_PREFIX.getInfo() + token);
    }

    @SneakyThrows
    @Override
    public byte[] download() {
        List<UserEntity> users = userRepository.findAll();
        return objectMapper.writeValueAsBytes(users);
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
        UserEntity userEntity =  userRepository.findByUsernameOrEmailOrPhone(username, username, username)
                .orElseThrow(() -> new MissException(USER_MISS.getMsg()));
        return UserEntityRpcVoConvertor.convert(userEntity);
    }
}
