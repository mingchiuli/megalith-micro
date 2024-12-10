package wiki.chiu.micro.user.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.convertor.RoleEntityConvertor;
import wiki.chiu.micro.user.convertor.RoleEntityRpcVoConvertor;
import wiki.chiu.micro.user.convertor.RoleEntityVoConvertor;
import wiki.chiu.micro.user.entity.RoleAuthorityEntity;
import wiki.chiu.micro.user.entity.RoleEntity;
import wiki.chiu.micro.user.entity.RoleMenuEntity;
import wiki.chiu.micro.user.entity.UserRoleEntity;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;
import wiki.chiu.micro.user.repository.*;
import wiki.chiu.micro.user.req.RoleEntityReq;
import wiki.chiu.micro.user.service.RoleService;
import wiki.chiu.micro.user.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.vo.RoleEntityVo;
import wiki.chiu.micro.user.wrapper.RoleMenuAuthorityWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private final RoleAuthorityRepository roleAuthorityRepository;

    private final UserRoleRepository userRoleRepository;

    private final RoleMenuAuthorityWrapper roleMenuAuthorityWrapper;

    private final ApplicationContext applicationContext;

    private final ExecutorService taskExecutor;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMenuRepository roleMenuRepository, RoleAuthorityRepository roleAuthorityRepository, UserRoleRepository userRoleRepository, RoleMenuAuthorityWrapper roleMenuAuthorityWrapper, ApplicationContext applicationContext, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.roleAuthorityRepository = roleAuthorityRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleMenuAuthorityWrapper = roleMenuAuthorityWrapper;
        this.applicationContext = applicationContext;
        this.taskExecutor = taskExecutor;
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
        List<RoleAuthorityEntity> roleAuthorities = roleAuthorityRepository.findByRoleIdIn(ids);

        return RoleEntityVoConvertor.convert(page, roleMenus, roleAuthorities);
    }

    @Override
    public void saveOrUpdate(RoleEntityReq roleReq) {

        Optional<Long> id = roleReq.id();
        RoleEntity roleEntity;

        if (id.isPresent()) {
            roleEntity = roleRepository.findById(id.get())
                    .orElseThrow(() -> new MissException(ROLE_NOT_EXIST));
        } else {
            roleEntity = new RoleEntity();
        }

        RoleEntityConvertor.convert(roleReq, roleEntity);

        roleRepository.save(roleEntity);
        //权限和按钮
        taskExecutor.execute(() -> {
            var authMenuIndexMessage = new AuthMenuIndexMessage(Collections.singletonList(roleEntity.getCode()), AuthMenuOperateEnum.AUTH_AND_MENU.getType());
            applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
        });
    }

    @Override
    public void delete(List<Long> ids) {
        roleMenuAuthorityWrapper.delete(ids);

        //多个角色删除
        taskExecutor.execute(() -> {
            var roles = roleRepository.findAllById(ids)
                    .stream()
                    .map(RoleEntity::getCode)
                    .distinct()
                    .toList();

            var authMenuIndexMessage = new AuthMenuIndexMessage(roles, AuthMenuOperateEnum.AUTH_AND_MENU.getType());
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
        List<RoleEntity> entities = roleRepository.findByStatus(StatusEnum.NORMAL.getCode());
        return RoleEntityVoConvertor.convert(entities);
    }

    @Override
    public List<RoleEntityRpcVo> findByRoleCodeInAndStatus(List<String> roles, Integer status) {
        List<RoleEntity> entities = roleRepository.findByCodeInAndStatus(roles, status);
        return RoleEntityRpcVoConvertor.convert(entities);
    }
}
