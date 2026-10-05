package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.util.List;

import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.export.SQLUtils;
import wiki.chiu.micro.user.application.model.AuthorityDraft;
import wiki.chiu.micro.user.application.model.SqlTables;
import wiki.chiu.micro.user.application.port.in.AuthorityService;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.application.port.out.AuthorityWriter;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.domain.Authority;
import wiki.chiu.micro.user.domain.MenuAuthority;

public class AuthorityServiceImpl implements AuthorityService {

    private final MenuAuthorityReader menuAuthorityReader;

    private final AuthorityReader authorityRepository;

    private final AuthorityWriter menuAuthorityWrapper;
    private final RoleReader roleRepository;

    public AuthorityServiceImpl(
        AuthorityReader authorityRepository,
        MenuAuthorityReader menuAuthorityReader,
        AuthorityWriter menuAuthorityWrapper,
        RoleReader roleRepository) {
        this.authorityRepository = authorityRepository;
        this.menuAuthorityReader = menuAuthorityReader;
        this.menuAuthorityWrapper = menuAuthorityWrapper;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Authority> findAllByService() {
        return authorityRepository.findByStatus(StatusEnum.NORMAL.getCode());
    }

    @Override
    public List<Authority> findAll() {
        return authorityRepository.findAll();
    }

    @Override
    public Authority findById(Long id) {
        return authorityRepository.findById(id).orElseThrow(() -> new MissException(NO_FOUND));
    }

    @Override
    public void saveOrUpdate(AuthorityDraft req) {
        Authority dealAuthority =
            req.id() == null
                ? Authority.blank()
                : authorityRepository.findById(req.id()).orElseGet(Authority::blank);

        menuAuthorityWrapper.saveAuthorityEntity(req.mergeInto(dealAuthority), findAllRoleIds());
    }

    @Override
    public void deleteAuthorities(List<Long> ids) {
        menuAuthorityWrapper.deleteAuthorities(ids, findAllRoleIds());
    }

    @Override
    public byte[] download() {
        List<Authority> authorities = authorityRepository.findAll();
        List<MenuAuthority> menuAuthorities = menuAuthorityReader.findAll();

        return SQLUtils.compose(
                SQLUtils.insertSql(authorities, SqlTables.AUTHORITY),
                SQLUtils.insertSql(menuAuthorities, SqlTables.MENU_AUTHORITY))
            .getBytes();
    }

    private List<Long> findAllRoleIds() {
        return roleRepository.findAll().stream().map(wiki.chiu.micro.user.domain.Role::id).toList();
    }
}
