package wiki.chiu.micro.user.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

import java.util.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.application.port.in.UserRoleService;
import wiki.chiu.micro.user.application.port.in.UserService;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.application.port.out.UserWriter;
import wiki.chiu.micro.user.config.convertor.UserEntityConvertor;
import wiki.chiu.micro.user.config.convertor.UserEntityVoConvertor;
import wiki.chiu.micro.user.domain.RoleEntity;
import wiki.chiu.micro.user.domain.UserEntity;
import wiki.chiu.micro.user.domain.UserRoleEntity;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.vo.UserEntityVo;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserReader userRepository;

    private final UserWriter userRoleWrapper;

    private final PasswordEncoder passwordEncoder;

    private final RoleReader roleRepository;

    private final UserRoleReader userRoleReader;

    private final UserRoleService userRoleService;

    public UserServiceImpl(
        UserReader userRepository,
        UserWriter userRoleWrapper,
        PasswordEncoder passwordEncoder,
        RoleReader roleRepository,
        UserRoleReader userRoleReader,
        UserRoleService userRoleService) {
        this.userRepository = userRepository;
        this.userRoleWrapper = userRoleWrapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleReader = userRoleReader;
        this.userRoleService = userRoleService;
    }

    @Override
    public UserEntityVo findInfo(Long userId) {
        UserEntity userEntity =
            userRepository.findById(userId).orElseThrow(() -> new MissException(USER_NOT_EXIST));

        List<String> roleCodes = userRoleService.findRoleCodesByUserId(userId);
        return UserEntityVoConvertor.convert(userEntity, roleCodes);
    }

    @Override
    public void saveOrUpdate(UserEntityReq userEntityReq) {

        UserEntity dealUser = getUserEntity(userEntityReq);

        UserEntityReq userReq =
            userEntityReq.id().isPresent() && !StringUtils.hasLength(userEntityReq.password())
                ? new UserEntityReq(userEntityReq, dealUser.getPassword())
                : new UserEntityReq(userEntityReq, passwordEncoder.encode(userEntityReq.password()));

        UserEntity userEntity = UserEntityConvertor.convert(userReq, dealUser);

        List<UserRoleEntity> userRoleEntities =
            roleRepository.findByCodeIn(userEntityReq.roles()).stream()
                .map(role -> UserRoleEntity.builder().roleId(role.getId()).build())
                .toList();

        userRoleWrapper.saveOrUpdate(userEntity, userRoleEntities);
    }

    @Override
    public PageAdapter<UserEntityVo> listPage(Integer currentPage, Integer size) {
        PageAdapter<UserEntity> page = userRepository.findPage(currentPage, size);

        List<Long> userIds = page.content().stream().map(UserEntity::getId).toList();
        List<UserRoleEntity> userRoleEntities = userRoleReader.findByUserIdIn(userIds);

        List<Long> roleIds = userRoleEntities.stream().map(UserRoleEntity::getRoleId).toList();
        List<RoleEntity> roleEntities = roleRepository.findAllById(roleIds);

        return UserEntityVoConvertor.convert(page, userRoleEntities, roleEntities);
    }

    @Override
    public void deleteUsers(List<Long> ids) {
        userRoleWrapper.deleteUsers(ids);
    }

    private UserEntity getUserEntity(UserEntityReq userEntityReq) {
        return userEntityReq.id().flatMap(userRepository::findById).orElseGet(UserEntity::new);
    }
}
