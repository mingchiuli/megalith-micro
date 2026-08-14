package wiki.chiu.micro.user.service.impl;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import java.util.List;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.convertor.AuthorityEntityConvertor;
import wiki.chiu.micro.user.convertor.AuthorityRpcVoConvertor;
import wiki.chiu.micro.user.convertor.AuthorityVoConvertor;
import wiki.chiu.micro.user.entity.AuthorityEntity;
import wiki.chiu.micro.user.entity.MenuAuthorityEntity;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.MenuAuthorityRepository;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.service.AuthorityService;
import wiki.chiu.micro.user.vo.AuthorityVo;
import wiki.chiu.micro.user.wrapper.MenuAuthorityWrapper;

@Service
public class AuthorityServiceImpl implements AuthorityService {

  private final MenuAuthorityRepository menuAuthorityRepository;

  private final AuthorityRepository authorityRepository;

  private final MenuAuthorityWrapper menuAuthorityWrapper;

  public AuthorityServiceImpl(
      AuthorityRepository authorityRepository,
      MenuAuthorityRepository menuAuthorityRepository,
      MenuAuthorityWrapper menuAuthorityWrapper) {
    this.authorityRepository = authorityRepository;
    this.menuAuthorityRepository = menuAuthorityRepository;
    this.menuAuthorityWrapper = menuAuthorityWrapper;
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
    menuAuthorityWrapper.authorityEntitySave(authorityEntity);
  }

  @Override
  public void deleteAuthorities(List<Long> ids) {
    menuAuthorityWrapper.deleteAuthorities(ids);
  }

  @Override
  public byte[] download() {
    List<AuthorityEntity> authorityEntities = authorityRepository.findAll();
    List<MenuAuthorityEntity> menuAuthorityEntities = menuAuthorityRepository.findAll();

    return SQLUtils.compose(
            SQLUtils.entityToInsertSQL(authorityEntities, Const.AUTHORITY_TABLE),
            SQLUtils.entityToInsertSQL(menuAuthorityEntities, Const.MENU_AUTHORITY_TABLE))
        .getBytes();
  }
}
