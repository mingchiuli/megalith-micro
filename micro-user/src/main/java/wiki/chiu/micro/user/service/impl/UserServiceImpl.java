package wiki.chiu.micro.user.service.impl;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.utils.CodeUtils;
import wiki.chiu.micro.common.utils.OssSignUtils;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.constant.UserIndexMessage;
import wiki.chiu.micro.user.convertor.UserEntityConvertor;
import wiki.chiu.micro.user.convertor.UserEntityRpcVoConvertor;
import wiki.chiu.micro.user.convertor.UserEntityVoConvertor;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.entity.UserRoleEntity;
import wiki.chiu.micro.user.event.UserOperateEvent;
import wiki.chiu.micro.user.lang.UserOperateEnum;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.req.UserEntityReq;
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
import wiki.chiu.micro.user.vo.UserEntityVo;
import wiki.chiu.micro.user.wrapper.UserRoleWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static wiki.chiu.micro.common.lang.Const.*;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;
import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

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

    private final UserRoleWrapper userRoleWrapper;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationContext applicationContext;

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;


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

    public UserServiceImpl(UserRepository userRepository, StringRedisTemplate redisTemplate, OssHttpService ossHttpService, @Qualifier("commonExecutor") ExecutorService taskExecutor, UserRoleWrapper userRoleWrapper, PasswordEncoder passwordEncoder, ApplicationContext applicationContext, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.ossHttpService = ossHttpService;
        this.taskExecutor = taskExecutor;
        this.userRoleWrapper = userRoleWrapper;
        this.passwordEncoder = passwordEncoder;
        this.applicationContext = applicationContext;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
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

    @Override
    public UserEntityVo findInfo(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new MissException(USER_NOT_EXIST));

        List<String> roleCodes = findRoleCodesByUserId(userId);
        return UserEntityVoConvertor.convert(userEntity, roleCodes);
    }

    @Override
    public void saveOrUpdate(UserEntityReq userEntityReq) {

        UserEntity dealUser = getUserEntity(userEntityReq);

        UserEntityReq userReq = userEntityReq.id().isPresent() && !StringUtils.hasLength(userEntityReq.password()) ?
                new UserEntityReq(userEntityReq, dealUser.getPassword()) :
                new UserEntityReq(userEntityReq, passwordEncoder.encode(userEntityReq.password()));

        UserEntity userEntity = UserEntityConvertor.convert(userReq, dealUser);

        List<UserRoleEntity> userRoleEntities = roleRepository.findByCodeIn(userEntityReq.roles()).stream()
                .map(role -> UserRoleEntity.builder()
                        .roleId(role.getId())
                        .build())
                .toList();

        userRoleWrapper.saveOrUpdate(userEntity, userRoleEntities);

        executeTask(dealUser.getId(), userEntityReq.id().isPresent() ? UserOperateEnum.UPDATE : UserOperateEnum.CREATE);
    }

    @Override
    public void saveRegisterPage(UserEntityRegisterReq req) {
        String phone = req.phone();

        UserEntityRegisterReq dealReq;
        if (!StringUtils.hasLength(phone)) {
            String fakePhone = CodeUtils.createPhone();
            dealReq = new UserEntityRegisterReq(req, fakePhone);
        } else {
            dealReq = req;
        }

        UserEntityReq userEntityReq = getUserEntityReq(dealReq);
        saveOrUpdate(userEntityReq);
        redisTemplate.delete(REGISTER_PREFIX + req.token());
    }

    @Override
    public PageAdapter<UserEntityVo> listPage(Integer currentPage, Integer size) {
        var pageRequest = PageRequest.of(currentPage - 1,
                size,
                Sort.by("created").ascending());
        Page<UserEntity> page = userRepository.findAll(pageRequest);

        List<Long> userIds = page.get()
                .map(UserEntity::getId)
                .toList();
        List<UserRoleEntity> userRoleEntities = userRoleRepository.findByUserIdIn(userIds);

        List<Long> roleIds = userRoleEntities.stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        List<RoleEntity> roleEntities = roleRepository.findAllById(roleIds);

        return UserEntityVoConvertor.convert(page, userRoleEntities, roleEntities);
    }

    @Override
    public void deleteUsers(List<Long> ids) {
        userRoleWrapper.deleteUsers(ids);
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

    private UserEntity getUserEntity(UserEntityReq userEntityReq) {
        return userEntityReq.id()
                .flatMap(userRepository::findById)
                .orElseGet(UserEntity::new);
    }

    private UserEntityReq getUserEntityReq(UserEntityRegisterReq req) {
        List<String> roles = List.of(USER, REFRESH);
        return userRepository.findByUsername(req.username())
                .map(entity -> new UserEntityReq(req, entity.getId(), NORMAL.getCode(), roles))
                .orElseGet(() -> new UserEntityReq(req, null, NORMAL.getCode(), roles));
    }

    private void executeTask(Long userId, UserOperateEnum userOperateEnum) {
        taskExecutor.execute(() -> {
            var userIndexMessage = new UserIndexMessage(userId, userOperateEnum);
            applicationContext.publishEvent(new UserOperateEvent(this, userIndexMessage));
        });
    }

    public List<String> findRoleCodesByUserId(Long userId) {
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        return roleRepository.findAllById(roleIds).stream()
                .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
                .map(RoleEntity::getCode)
                .toList();
    }
}
