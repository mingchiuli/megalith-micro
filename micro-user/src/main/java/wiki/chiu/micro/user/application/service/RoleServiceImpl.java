package wiki.chiu.micro.user.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.ROLE_NOT_EXIST;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.application.port.in.RoleService;
import wiki.chiu.micro.user.application.port.out.*;
import wiki.chiu.micro.user.application.port.out.RoleWriter;
import wiki.chiu.micro.user.config.convertor.RoleEntityConvertor;
import wiki.chiu.micro.user.config.convertor.RoleEntityRpcVoConvertor;
import wiki.chiu.micro.user.config.convertor.RoleEntityVoConvertor;
import wiki.chiu.micro.user.domain.*;
import wiki.chiu.micro.user.req.RoleEntityReq;
import wiki.chiu.micro.user.vo.RoleEntityVo;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:26 am
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleReader roleRepository;

    private final RoleMenuReader roleMenuReader;

    private final UserRoleReader userRoleReader;

    private final RoleDataPermissionReader roleDataPermissionRepository;

    private final RoleWriter roleWrapper;

    private final AuthorizationQueryService authorizationQueries;

    public RoleServiceImpl(
        RoleReader roleRepository,
        RoleMenuReader roleMenuReader,
        UserRoleReader userRoleReader,
        RoleDataPermissionReader roleDataPermissionRepository,
        RoleWriter roleWrapper,
        AuthorizationQueryService authorizationQueries) {
        this.roleRepository = roleRepository;
        this.roleMenuReader = roleMenuReader;
        this.userRoleReader = userRoleReader;
        this.roleDataPermissionRepository = roleDataPermissionRepository;
        this.roleWrapper = roleWrapper;
        this.authorizationQueries = authorizationQueries;
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
        PageAdapter<RoleEntity> page = roleRepository.findPage(currentPage, size);

        List<Long> ids = page.content().stream().map(RoleEntity::getId).toList();

        if (ids.isEmpty()) {
            return RoleEntityVoConvertor.convert(page, List.of(), List.of());
        }

        List<RoleMenuEntity> roleMenus = roleMenuReader.findByRoleIdIn(ids);
        List<RoleDataPermissionEntity> dataPermissions =
            roleDataPermissionRepository.findByRoleIdIn(ids);
        return RoleEntityVoConvertor.convert(page, roleMenus, dataPermissions);
    }

    @Override
    public void saveOrUpdate(RoleEntityReq roleReq) {

        RoleEntity dealRole = roleReq.id().flatMap(roleRepository::findById).orElseGet(RoleEntity::new);
        String previousCode = dealRole.getCode();
        RoleEntity roleEntity = RoleEntityConvertor.convert(roleReq, dealRole);
        List<String> affectedCodes =
            Stream.of(previousCode, roleEntity.getCode()).filter(Objects::nonNull).distinct().toList();
        roleWrapper.saveOrUpdate(roleEntity, affectedCodes);
    }

    @Override
    public void delete(List<Long> ids) {
        List<String> roles =
            roleRepository.findAllById(ids).stream().map(RoleEntity::getCode).distinct().toList();
        roleWrapper.delete(ids, roles);
    }

    @Override
    public byte[] download() {
        List<RoleEntity> roleEntities = roleRepository.findAll();
        List<UserRoleEntity> userRoleEntities = userRoleReader.findAll();
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

    @Override
    public List<RoleAuthorizationRpcVo> findAllRoleAuthorizations() {
        return authorizationQueries.findAllRoleAuthorizations();
    }

    @Override
    public List<RoleAuthorizationRpcVo> findRoleAuthorizations(List<Long> roleIds) {
        return authorizationQueries.findRoleAuthorizations(roleIds);
    }
}
