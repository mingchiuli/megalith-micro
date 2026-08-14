package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.ROLE_NOT_EXIST;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.convertor.RoleEntityConvertor;
import wiki.chiu.micro.user.convertor.RoleEntityRpcVoConvertor;
import wiki.chiu.micro.user.convertor.RoleEntityVoConvertor;
import wiki.chiu.micro.user.entity.*;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;
import wiki.chiu.micro.user.repository.*;
import wiki.chiu.micro.user.req.RoleEntityReq;
import wiki.chiu.micro.user.service.RoleService;
import wiki.chiu.micro.user.vo.RoleEntityVo;
import wiki.chiu.micro.user.wrapper.UserRoleMenuWrapper;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:26 am
 */
@Service
public class RoleServiceImpl implements RoleService {

  private final RoleRepository roleRepository;

  private final RoleMenuRepository roleMenuRepository;

  private final UserRoleRepository userRoleRepository;

  private final RoleDataPermissionRepository roleDataPermissionRepository;

  private final ApplicationEventPublisher eventPublisher;

  private final UserRoleMenuWrapper userRoleMenuWrapper;

  public RoleServiceImpl(
      RoleRepository roleRepository,
      RoleMenuRepository roleMenuRepository,
      UserRoleRepository userRoleRepository,
      RoleDataPermissionRepository roleDataPermissionRepository,
      ApplicationEventPublisher eventPublisher,
      UserRoleMenuWrapper userRoleMenuWrapper) {
    this.roleRepository = roleRepository;
    this.roleMenuRepository = roleMenuRepository;
    this.userRoleRepository = userRoleRepository;
    this.roleDataPermissionRepository = roleDataPermissionRepository;
    this.eventPublisher = eventPublisher;
    this.userRoleMenuWrapper = userRoleMenuWrapper;
  }

  @Override
  public RoleEntityVo info(Long id) {
    RoleEntity roleEntity =
        roleRepository.findById(id).orElseThrow(() -> new MissException(ROLE_NOT_EXIST));

    return RoleEntityVoConvertor.convert(
        roleEntity, roleDataPermissionRepository.findByRoleId(roleEntity.getId()));
  }

  @Override
  public PageAdapter<RoleEntityVo> getPage(Integer currentPage, Integer size) {
    var pageRequest = PageRequest.of(currentPage - 1, size, Sort.by("created").ascending());
    Page<@NonNull RoleEntity> page = roleRepository.findAll(pageRequest);

    List<Long> ids = page.get().map(RoleEntity::getId).toList();

    if (ids.isEmpty()) {
      return RoleEntityVoConvertor.convert(page, List.of(), List.of());
    }

    List<RoleMenuEntity> roleMenus = roleMenuRepository.findByRoleIdIn(ids);
    List<RoleDataPermissionEntity> dataPermissions =
        roleDataPermissionRepository.findByRoleIdIn(ids);
    return RoleEntityVoConvertor.convert(page, roleMenus, dataPermissions);
  }

  @Override
  @Transactional
  public void saveOrUpdate(RoleEntityReq roleReq) {

    RoleEntity dealRole = roleReq.id().flatMap(roleRepository::findById).orElseGet(RoleEntity::new);
    String previousCode = dealRole.getCode();
    RoleEntity roleEntity = RoleEntityConvertor.convert(roleReq, dealRole);
    RoleEntity savedRole = roleRepository.save(roleEntity);
    roleDataPermissionRepository.deleteByRoleId(savedRole.getId());
    roleDataPermissionRepository.flush();
    roleDataPermissionRepository.saveAll(
        roleReq.dataPermissions().stream()
            .distinct()
            .sorted()
            .map(permission -> new RoleDataPermissionEntity(savedRole.getId(), permission))
            .toList());
    List<String> affectedCodes =
        Stream.of(previousCode, roleEntity.getCode()).filter(Objects::nonNull).distinct().toList();
    executeDelRolesAuthTask(affectedCodes, AuthMenuOperateEnum.AUTH_AND_MENU.getType());
  }

  @Override
  @Transactional
  public void delete(List<Long> ids) {
    List<String> roles =
        roleRepository.findAllById(ids).stream().map(RoleEntity::getCode).distinct().toList();
    userRoleMenuWrapper.deleteRole(ids);

    executeDelRolesAuthTask(roles, AuthMenuOperateEnum.AUTH_AND_MENU.getType());
  }

  private void executeDelRolesAuthTask(List<String> roles, Integer type) {
    var authMenuIndexMessage = new AuthMenuIndexMessage(roles, type);
    eventPublisher.publishEvent(new AuthMenuOperateEvent(authMenuIndexMessage));
  }

  @Override
  public byte[] download() {
    List<RoleEntity> roleEntities = roleRepository.findAll();
    List<UserRoleEntity> userRoleEntities = userRoleRepository.findAll();
    List<RoleDataPermissionEntity> dataPermissions = roleDataPermissionRepository.findAll();

    return SQLUtils.compose(
            SQLUtils.entityToInsertSQL(roleEntities, Const.ROLE_TABLE),
            SQLUtils.entityToInsertSQL(userRoleEntities, Const.USER_ROLE_TABLE),
            SQLUtils.entityToInsertSQL(dataPermissions, Const.ROLE_DATA_PERMISSION_TABLE))
        .getBytes();
  }

  @Override
  public List<RoleEntityVo> getValidAll() {
    List<RoleEntity> entities =
        roleRepository.findAll().stream()
            .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
            .toList();
    return RoleEntityVoConvertor.convert(entities);
  }

  @Override
  public List<RoleEntityRpcVo> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
    List<RoleEntity> entities = roleRepository.findByCodeInAndStatus(roles, status);
    return RoleEntityRpcVoConvertor.convert(entities);
  }
}
