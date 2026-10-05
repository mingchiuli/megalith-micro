package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.enums.StatusEnum.NORMAL;

import java.util.List;

import wiki.chiu.micro.common.enums.AuthTypeEnum;
import wiki.chiu.micro.user.application.model.MenuAuthorityView;
import wiki.chiu.micro.user.application.port.in.MenuAuthorityService;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.application.port.out.AuthorityWriter;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.MenuAuthority;
import wiki.chiu.micro.user.domain.Role;

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
        List<MenuAuthority> menuAuthorities =
            authorityIds.stream()
                .map(authorityId -> new MenuAuthority(null, menuId, authorityId, null, null))
                .toList();
        List<Long> roleIds = roleRepository.findAll().stream().map(Role::id).toList();
        menuAuthorityWrapper.saveAuthority(menuId, menuAuthorities, roleIds);
    }

    @Override
    public List<MenuAuthorityView> getAuthoritiesInfo(Long menuId) {
        List<Long> ids =
            menuAuthorityReader.findByMenuId(menuId).stream()
                .map(MenuAuthority::authorityId)
                .toList();

        return authorityRepository.findByStatusAndType(
                NORMAL.getCode(), AuthTypeEnum.NEED_AUTH.getCode())
            .stream()
            .map(authority -> toView(authority, ids))
            .toList();
    }

    private static MenuAuthorityView toView(Authority authority, List<Long> ids) {
        return new MenuAuthorityView(
            authority.id(), authority.code(), ids.contains(authority.id()));
    }
}
