package wiki.chiu.micro.user.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.common.vo.RoleEntityRpcVo;
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
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.user.wrapper.RoleMenuWrapper;
import wiki.chiu.micro.user.wrapper.UserRoleMenuWrapper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static wiki.chiu.micro.common.lang.ExceptionMessage.ROLE_NOT_EXIST;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:26 am
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private final RoleMenuRepository roleMenuRepository;

    private final UserRoleRepository userRoleRepository;

    private final ApplicationContext applicationContext;

    private final ExecutorService taskExecutor;

    private final UserRoleMenuWrapper userRoleMenuWrapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMenuRepository roleMenuRepository, UserRoleRepository userRoleRepository, RoleMenuWrapper roleMenuWrapper, ApplicationContext applicationContext, @Qualifier("commonExecutor") ExecutorService taskExecutor, UserRoleMenuWrapper userRoleMenuWrapper) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.userRoleRepository = userRoleRepository;
        this.applicationContext = applicationContext;
        this.taskExecutor = taskExecutor;
        this.userRoleMenuWrapper = userRoleMenuWrapper;
    }

    @Override
    public RoleEntityVo info(Long id) {
        RoleEntity roleEntity = roleRepository.findById(id)
                .orElseThrow(() -> new MissException(ROLE_NOT_EXIST));

        return RoleEntityVoConvertor.convert(roleEntity);
    }

    @Override
    public PageAdapter<RoleEntityVo> getPage(Integer currentPage, Integer size) {
        var pageRequest = PageRequest.of(currentPage - 1,
                size,
                Sort.by("created").ascending());
        Page<RoleEntity> page = roleRepository.findAll(pageRequest);

        List<Long> ids = page.get().map(RoleEntity::getId).toList();

        List<RoleMenuEntity> roleMenus = roleMenuRepository.findByRoleIdIn(ids);
        return RoleEntityVoConvertor.convert(page, roleMenus);
    }

    @Override
    public void saveOrUpdate(RoleEntityReq roleReq) {

        RoleEntity dealRole = roleReq.id()
                .flatMap(roleRepository::findById)
                .orElseGet(RoleEntity::new);
        RoleEntity roleEntity = RoleEntityConvertor.convert(roleReq, dealRole);
        roleRepository.save(roleEntity);
        executeDelRolesAuthTask(Collections.singletonList(roleEntity.getCode()), AuthMenuOperateEnum.AUTH_AND_MENU.getType());
    }

    @Override
    public void delete(List<Long> ids) {
        userRoleMenuWrapper.deleteRole(ids);
        List<String> roles = roleRepository.findAllById(ids).stream()
                .map(RoleEntity::getCode)
                .distinct()
                .toList();

        //多个角色删除
        executeDelRolesAuthTask(roles, AuthMenuOperateEnum.AUTH_AND_MENU.getType());
    }

    private void executeDelRolesAuthTask(List<String> roles, Integer type) {
        taskExecutor.execute(() -> {
            var authMenuIndexMessage = new AuthMenuIndexMessage(roles, type);
            applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
        });
    }

    @Override
    public byte[] download() {
        List<RoleEntity> roleEntities = roleRepository.findAll();
        List<UserRoleEntity> userRoleEntities = userRoleRepository.findAll();

        return SQLUtils.compose(
                SQLUtils.entityToInsertSQL(roleEntities, Const.ROLE_TABLE),
                SQLUtils.entityToInsertSQL(userRoleEntities, Const.USER_ROLE_TABLE))
                .getBytes();
    }

    @Override
    public List<RoleEntityVo> getValidAll() {
        List<RoleEntity> entities = roleRepository.findAll().stream()
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
