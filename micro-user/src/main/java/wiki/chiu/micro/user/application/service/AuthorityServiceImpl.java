package wiki.chiu.micro.user.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import java.util.List;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.application.port.in.AuthorityService;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.application.port.out.AuthorityWriter;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.config.convertor.AuthorityEntityConvertor;
import wiki.chiu.micro.user.config.convertor.AuthorityRpcVoConvertor;
import wiki.chiu.micro.user.config.convertor.AuthorityVoConvertor;
import wiki.chiu.micro.user.domain.AuthorityEntity;
import wiki.chiu.micro.user.domain.MenuAuthorityEntity;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.vo.AuthorityVo;

@Service
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
    public List<AuthorityRpcVo> findAllByService() {
        List<AuthorityEntity> authorityEntities =
            authorityRepository.findAll().stream()
                .filter(item -> StatusEnum.NORMAL.getCode().equals(item.getStatus()))
                .toList();
        return AuthorityRpcVoConvertor.convert(authorityEntities);
    }

    @Override
    public List<AuthorityVo> findAll() {
        List<AuthorityEntity> authorityEntities = authorityRepository.findAll();
        return AuthorityVoConvertor.convert(authorityEntities);
    }

    @Override
    public AuthorityVo findById(Long id) {
        AuthorityEntity authorityEntity =
            authorityRepository.findById(id).orElseThrow(() -> new MissException(NO_FOUND));
        return AuthorityVoConvertor.convert(authorityEntity);
    }

    @Override
    public void saveOrUpdate(AuthorityEntityReq req) {
        AuthorityEntity dealAuthority =
            req.id().flatMap(authorityRepository::findById).orElseGet(AuthorityEntity::new);

        AuthorityEntity authorityEntity = AuthorityEntityConvertor.convert(req, dealAuthority);
        menuAuthorityWrapper.saveAuthorityEntity(authorityEntity, findAllRoleIds());
    }

    @Override
    public void deleteAuthorities(List<Long> ids) {
        menuAuthorityWrapper.deleteAuthorities(ids, findAllRoleIds());
    }

    @Override
    public byte[] download() {
        List<AuthorityEntity> authorityEntities = authorityRepository.findAll();
        List<MenuAuthorityEntity> menuAuthorityEntities = menuAuthorityReader.findAll();

        return SQLUtils.compose(
                SQLUtils.entityToInsertSQL(authorityEntities, Const.AUTHORITY_TABLE),
                SQLUtils.entityToInsertSQL(menuAuthorityEntities, Const.MENU_AUTHORITY_TABLE))
            .getBytes();
    }

    private List<Long> findAllRoleIds() {
        return roleRepository.findAll().stream().map(role -> role.getId()).toList();
    }
}
