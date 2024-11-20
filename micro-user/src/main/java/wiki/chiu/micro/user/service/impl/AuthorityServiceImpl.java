package wiki.chiu.micro.user.service.impl;

import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.AuthMenuOperateEnum;
import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.common.lang.StatusEnum;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.user.constant.AuthMenuIndexMessage;
import wiki.chiu.micro.user.convertor.AuthorityEntityConvertor;
import wiki.chiu.micro.user.convertor.AuthorityRpcVoConvertor;
import wiki.chiu.micro.user.convertor.AuthorityVoConvertor;
import wiki.chiu.micro.user.entity.AuthorityEntity;
import wiki.chiu.micro.user.entity.RoleAuthorityEntity;
import wiki.chiu.micro.user.event.AuthMenuOperateEvent;
import wiki.chiu.micro.user.repository.AuthorityRepository;
import wiki.chiu.micro.user.repository.RoleAuthorityRepository;
import wiki.chiu.micro.user.repository.RoleRepository;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.service.AuthorityService;
import wiki.chiu.micro.user.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.vo.AuthorityVo;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;


@Service
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    private final RoleAuthorityRepository roleAuthorityRepository;

    public AuthorityServiceImpl(AuthorityRepository authorityRepository, RoleRepository roleRepository, ApplicationContext applicationContext, RoleAuthorityRepository roleAuthorityRepository) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
        this.roleAuthorityRepository = roleAuthorityRepository;
    }

    @Override
    public List<AuthorityRpcVo> findAllByService(List<String> service) {
        List<AuthorityEntity> authorityEntities = authorityRepository.findByServiceHostInAndStatus(service, StatusEnum.NORMAL.getCode());
        return AuthorityRpcVoConvertor.convert(authorityEntities);
    }

    @Override
    public List<AuthorityVo> findAll() {
        List<AuthorityEntity> authorityEntities = authorityRepository.findAll();
        return AuthorityVoConvertor.convert(authorityEntities);
    }

    @Override
    public AuthorityVo findById(Long id) {
        AuthorityEntity authorityEntity = authorityRepository.findById(id)
                .orElseThrow(() -> new MissException(NO_FOUND));
        return AuthorityVoConvertor.convert(authorityEntity);
    }

    @Override
    public void saveOrUpdate(AuthorityEntityReq req) {

        Optional<Long> id = req.id();
        AuthorityEntity authorityEntity;

        if (id.isPresent()) {
            authorityEntity = authorityRepository.findById(id.get())
                    .orElseThrow(() -> new MissException(NO_FOUND));
        } else {
            authorityEntity = new AuthorityEntity();
        }

        AuthorityEntityConvertor.convert(req, authorityEntity);
        authorityRepository.save(authorityEntity);

        //全部权限
        List<String> allRoleCodes = roleRepository.findAllCodes();
        var authMenuIndexMessage = new AuthMenuIndexMessage(allRoleCodes, AuthMenuOperateEnum.AUTH.getType());
        applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
    }

    @Override
    public void deleteAuthorities(List<Long> ids) {

        authorityRepository.deleteAllById(ids);
        //全部权限
        List<String> allRoleCodes = roleRepository.findAllCodes();
        var authMenuIndexMessage = new AuthMenuIndexMessage(allRoleCodes, AuthMenuOperateEnum.AUTH.getType());
        applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
    }

    @Override
    public byte[] download() {
        List<AuthorityEntity> authorityEntities = authorityRepository.findAll();
        List<RoleAuthorityEntity> roleAuthorityEntities = roleAuthorityRepository.findAll();

        String authorities = SQLUtils.entityToInsertSQL(authorityEntities, Const.AUTHORITY_TABLE);
        String roleAuthorities = SQLUtils.entityToInsertSQL(roleAuthorityEntities, Const.ROLE_AUTHORITY_TABLE);

        return (authorities + "\n" + roleAuthorities).getBytes();
    }
}
