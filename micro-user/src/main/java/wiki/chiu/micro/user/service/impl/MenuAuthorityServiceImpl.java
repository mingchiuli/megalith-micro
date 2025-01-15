package wiki.chiu.micro.user.service.impl;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.convertor.MenuAuthorityEntityConvertor;
import wiki.chiu.micro.user.convertor.MenuAuthorityVoConvertor;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.service.MenuAuthorityService;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;
import wiki.chiu.micro.user.wrapper.MenuAuthorityWrapper;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

@Service
public class MenuAuthorityServiceImpl implements MenuAuthorityService {

    private final MenuAuthorityWrapper menuAuthorityWrapper;

    private final RoleRepository roleRepository;

    private final ExecutorService taskExecutor;

    private final ApplicationContext applicationContext;

    private final MenuAuthorityRepository menuAuthorityRepository;

    private final AuthorityRepository authorityRepository;

    public MenuAuthorityServiceImpl(MenuAuthorityWrapper menuAuthorityWrapper, RoleRepository roleRepository, ExecutorService taskExecutor, ApplicationContext applicationContext, MenuAuthorityRepository menuAuthorityRepository, AuthorityRepository authorityRepository) {
        this.menuAuthorityWrapper = menuAuthorityWrapper;
        this.roleRepository = roleRepository;
        this.taskExecutor = taskExecutor;
        this.applicationContext = applicationContext;
        this.menuAuthorityRepository = menuAuthorityRepository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    public void saveAuthority(Long menuId, List<Long> authorityIds) {
        List<MenuAuthorityEntity> roleAuthorityEntities = MenuAuthorityEntityConvertor.convert(menuId, authorityIds);
        menuAuthorityWrapper.saveAuthority(menuId, roleAuthorityEntities);
        // 删除权限缓存
        executeDelMenuAuthTask();
    }

    private void executeDelMenuAuthTask() {
        taskExecutor.execute(() -> {
            List<String> allRoleCodes = roleRepository.findAllCodes();
            var authMenuIndexMessage = new AuthMenuIndexMessage(allRoleCodes, AuthMenuOperateEnum.AUTH.getType());
            applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
        });
    }

    @Override
    public List<MenuAuthorityVo> getAuthoritiesInfo(Long menuId) {
        List<Long> ids = menuAuthorityRepository.findByMenuId(menuId).stream()
                .map(MenuAuthorityEntity::getAuthorityId)
                .toList();

        return authorityRepository.findAll().stream()
                .filter(item -> NORMAL.getCode().equals(item.getStatus()))
                .filter(item -> AuthTypeEnum.NEED_AUTH.getCode().equals(item.getType()))
                .map(item -> MenuAuthorityVoConvertor.convert(item, ids))
                .toList();
    }
}
