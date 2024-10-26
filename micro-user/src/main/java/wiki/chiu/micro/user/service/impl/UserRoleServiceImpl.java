package wiki.chiu.micro.user.service.impl;

import wiki.chiu.micro.common.exception.CommitException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.code.CodeFactory;
import wiki.chiu.micro.user.constant.UserIndexMessage;
import wiki.chiu.micro.user.convertor.UserEntityConvertor;
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
import wiki.chiu.micro.user.service.UserRoleService;
import wiki.chiu.micro.user.vo.UserEntityVo;
import wiki.chiu.micro.user.wrapper.UserRoleWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static wiki.chiu.micro.common.lang.Const.REGISTER_PREFIX;
import static wiki.chiu.micro.common.lang.Const.USER;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;
import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

/**
 * @Author limingjiu
 * @Date 2024/5/29 22:12
 **/
@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final RoleRepository roleRepository;

    private final CodeFactory codeFactory;

    private final StringRedisTemplate redisTemplate;

    private final UserRoleWrapper userRoleWrapper;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationContext applicationContext;

    private final UserRoleRepository userRoleRepository;


    public UserRoleServiceImpl(RoleRepository roleRepository, CodeFactory codeFactory, StringRedisTemplate redisTemplate, UserRoleWrapper userRoleWrapper, UserRepository userRepository, PasswordEncoder passwordEncoder, ApplicationContext applicationContext, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.codeFactory = codeFactory;
        this.redisTemplate = redisTemplate;
        this.userRoleWrapper = userRoleWrapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationContext = applicationContext;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public void saveOrUpdate(UserEntityReq userEntityReq) {
        Optional<Long> id = userEntityReq.id();
        List<String> roles = userEntityReq.roles();
        UserEntity userEntity;
        UserOperateEnum userOperateEnum;

        if (id.isPresent()) {
            userEntity = userRepository.findById(id.get())
                    .orElseThrow(() -> new MissException(USER_NOT_EXIST));

            String password = userEntityReq.password();
            if (StringUtils.hasLength(password)) {
                userEntityReq = new UserEntityReq(userEntityReq, passwordEncoder.encode(password));
            } else {
                userEntityReq = new UserEntityReq(userEntityReq, userEntity.getPassword());
            }
            userOperateEnum = UserOperateEnum.UPDATE;
        } else {
            userEntity = new UserEntity();
            userEntityReq = new UserEntityReq(userEntityReq, passwordEncoder.encode(Optional.ofNullable(userEntityReq.password())
                    .orElseThrow(() -> new CommitException(PASSWORD_REQUIRED))));
            userOperateEnum = UserOperateEnum.CREATE;
        }

        UserEntityConvertor.convert(userEntityReq, userEntity);

        List<UserRoleEntity> userRoleEntities = roleRepository.findByCodeIn(roles).stream()
                .map(role -> UserRoleEntity.builder()
                        .roleId(role.getId())
                        .build())
                .toList();

        userRoleWrapper.saveOrUpdate(userEntity, userRoleEntities);

        var userIndexMessage = new UserIndexMessage(userEntity.getId(), userOperateEnum);
        applicationContext.publishEvent(new UserOperateEvent(this, userIndexMessage));
    }

    @Override
    public void saveRegisterPage(String token, UserEntityRegisterReq userEntityRegisterReq) {
        Boolean exist = redisTemplate.hasKey(REGISTER_PREFIX + token);
        if (Boolean.FALSE.equals(exist)) {
            throw new MissException(NO_AUTH.getMsg());
        }
        String password = userEntityRegisterReq.password();
        String confirmPassword = userEntityRegisterReq.confirmPassword();
        if (!Objects.equals(confirmPassword, password)) {
            throw new MissException(PASSWORD_DIFF.getMsg());
        }

        String phone = userEntityRegisterReq.phone();
        if (!StringUtils.hasLength(phone)) {
            String fakePhone = codeFactory.createPhone();
            userEntityRegisterReq = new UserEntityRegisterReq(userEntityRegisterReq, fakePhone);
        }

        String username = userEntityRegisterReq.username();
        String usernameCopy = redisTemplate.opsForValue().get(REGISTER_PREFIX + token);
        if (StringUtils.hasLength(usernameCopy) && !Objects.equals(usernameCopy, username)) {
            throw new MissException(NO_AUTH.getMsg());
        }

        UserEntityReq userEntityReq;

        Optional<UserEntity> userEntity = userRepository.findByUsernameAndStatus(username, NORMAL.getCode());
        if (userEntity.isEmpty()) {
            userEntityReq = new UserEntityReq(userEntityRegisterReq, null, NORMAL.getCode(), Collections.singletonList(USER));
        } else {
            userEntityReq = new UserEntityReq(userEntityRegisterReq, userEntity.get().getId(),  NORMAL.getCode(), Collections.singletonList(USER));
        }
        saveOrUpdate(userEntityReq);
        redisTemplate.delete(REGISTER_PREFIX + token);
    }

    @Override
    public List<String> findRoleCodesByUserId(Long userId) {
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        return roleRepository.findAllById(roleIds).stream()
                .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
                .map(RoleEntity::getCode)
                .toList();
    }

    @Override
    public UserEntityVo findById(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new MissException(USER_NOT_EXIST));

        List<String> roleCodes = findRoleCodesByUserId(userId);
        return UserEntityVoConvertor.convert(userEntity, roleCodes);
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
}
