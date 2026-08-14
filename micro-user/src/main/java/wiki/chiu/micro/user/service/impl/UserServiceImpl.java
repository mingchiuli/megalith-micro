package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

import java.util.*;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.convertor.UserEntityConvertor;
import wiki.chiu.micro.user.convertor.UserEntityVoConvertor;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.entity.UserEntity;
import wiki.chiu.micro.user.entity.UserRoleEntity;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.repository.UserRepository;
import wiki.chiu.micro.user.repository.UserRoleRepository;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.service.UserRoleService;
import wiki.chiu.micro.user.service.UserService;
import wiki.chiu.micro.user.vo.UserEntityVo;
import wiki.chiu.micro.user.wrapper.UserRoleWrapper;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final UserRoleWrapper userRoleWrapper;

  private final PasswordEncoder passwordEncoder;

  private final RoleRepository roleRepository;

  private final UserRoleRepository userRoleRepository;

  private final UserRoleService userRoleService;

  public UserServiceImpl(
      UserRepository userRepository,
      UserRoleWrapper userRoleWrapper,
      PasswordEncoder passwordEncoder,
      RoleRepository roleRepository,
      UserRoleRepository userRoleRepository,
      UserRoleService userRoleService) {
    this.userRepository = userRepository;
    this.userRoleWrapper = userRoleWrapper;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
    this.userRoleRepository = userRoleRepository;
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
    var pageRequest = PageRequest.of(currentPage - 1, size, Sort.by("created").ascending());
    Page<@NonNull UserEntity> page = userRepository.findAll(pageRequest);

    List<Long> userIds = page.get().map(UserEntity::getId).toList();
    List<UserRoleEntity> userRoleEntities = userRoleRepository.findByUserIdIn(userIds);

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
