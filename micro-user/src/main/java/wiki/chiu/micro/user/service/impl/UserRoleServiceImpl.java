package wiki.chiu.micro.user.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.common.exception.CommitException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.utils.CodeUtils;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static wiki.chiu.micro.common.lang.Const.*;
import static wiki.chiu.micro.common.lang.ExceptionMessage.*;
import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

/**
 * @Author limingjiu
 * @Date 2024/5/29 22:12
 **/
@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final RoleRepository roleRepository;

    private final StringRedisTemplate redisTemplate;

    private final UserRoleWrapper userRoleWrapper;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationContext applicationContext;

    private final UserRoleRepository userRoleRepository;

    private final ExecutorService taskExecutor;

    public UserRoleServiceImpl(RoleRepository roleRepository, StringRedisTemplate redisTemplate, UserRoleWrapper userRoleWrapper, UserRepository userRepository, PasswordEncoder passwordEncoder, ApplicationContext applicationContext, UserRoleRepository userRoleRepository, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.roleRepository = roleRepository;
        this.redisTemplate = redisTemplate;
        this.userRoleWrapper = userRoleWrapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationContext = applicationContext;
        this.userRoleRepository = userRoleRepository;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void saveOrUpdate(UserEntityReq userEntityReq) {

        UserEntity userEntity = getUserEntity(userEntityReq);

        UserOperateEnum userOperateEnum;
        UserEntityReq userReq;
        if (userEntityReq.id().isPresent()) {

            String password = userEntityReq.password();
            if (StringUtils.hasLength(password)) {
                userReq = new UserEntityReq(userEntityReq, passwordEncoder.encode(password));
            } else {
                userReq = new UserEntityReq(userEntityReq, userEntity.getPassword());
            }
            userOperateEnum = UserOperateEnum.UPDATE;
        } else {
            userReq = new UserEntityReq(userEntityReq, passwordEncoder.encode(Optional.ofNullable(userEntityReq.password())
                    .orElseThrow(() -> new CommitException(PASSWORD_REQUIRED))));
            userOperateEnum = UserOperateEnum.CREATE;
        }

        UserEntityConvertor.convert(userReq, userEntity);

        List<UserRoleEntity> userRoleEntities = roleRepository.findByCodeIn(userEntityReq.roles()).stream()
                .map(role -> UserRoleEntity.builder()
                        .roleId(role.getId())
                        .build())
                .toList();

        userRoleWrapper.saveOrUpdate(userEntity, userRoleEntities);

        executeTask(userEntity.getId(), userOperateEnum);
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
}
