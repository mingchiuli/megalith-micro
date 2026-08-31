package wiki.chiu.micro.user.application.service;

import static wiki.chiu.micro.common.lang.StatusEnum.NORMAL;

import java.util.List;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.common.lang.AuthTypeEnum;
import wiki.chiu.micro.user.application.port.in.MenuAuthorityService;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.application.port.out.AuthorityWriter;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.config.convertor.MenuAuthorityEntityConvertor;
import wiki.chiu.micro.user.config.convertor.MenuAuthorityVoConvertor;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;

@Service
public class MenuAuthorityServiceImpl implements MenuAuthorityService {

    private final AuthorityWriter menuAuthorityWrapper;

    private final MenuAuthorityReader menuAuthorityReader;

    private final AuthorityReader authorityRepository;
    private final RoleReader roleRepository;

    public MenuAuthorityServiceImpl(
        AuthorityWriter menuAuthorityWrapper,
        MenuAuthorityReader menuAuthorityReader,
        AuthorityReader authorityRepository,
        RoleReader roleRepository) {
        this.menuAuthorityWrapper = menuAuthorityWrapper;
        this.menuAuthorityReader = menuAuthorityReader;
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void saveAuthority(Long menuId, List<Long> authorityIds) {
        List<MenuAuthorityEntity> roleAuthorityEntities =
            MenuAuthorityEntityConvertor.convert(menuId, authorityIds);
        List<Long> roleIds = roleRepository.findAll().stream().map(role -> role.getId()).toList();
        menuAuthorityWrapper.saveAuthority(menuId, roleAuthorityEntities, roleIds);
    }

    @Override
    public List<MenuAuthorityVo> getAuthoritiesInfo(Long menuId) {
        List<Long> ids =
            menuAuthorityReader.findByMenuId(menuId).stream()
                .map(MenuAuthorityEntity::getAuthorityId)
                .toList();

        return authorityRepository.findAll().stream()
            .filter(item -> NORMAL.getCode().equals(item.getStatus()))
            .filter(item -> AuthTypeEnum.NEED_AUTH.getCode().equals(item.getType()))
            .map(item -> MenuAuthorityVoConvertor.convert(item, ids))
            .toList();
    }
}
