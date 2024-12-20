package wiki.chiu.micro.user.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.concurrent.ExecutorService;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;


@Service
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    private final RoleAuthorityRepository roleAuthorityRepository;

    private final ExecutorService taskExecutor;

    public AuthorityServiceImpl(AuthorityRepository authorityRepository, RoleRepository roleRepository, ApplicationContext applicationContext, RoleAuthorityRepository roleAuthorityRepository, @Qualifier("commonExecutor") ExecutorService taskExecutor) {
        this.authorityRepository = authorityRepository;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
        this.roleAuthorityRepository = roleAuthorityRepository;
        this.taskExecutor = taskExecutor;
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
        AuthorityEntity authorityEntity = req.id()
                .flatMap(authorityRepository::findById)
                .orElseGet(AuthorityEntity::new);

        AuthorityEntityConvertor.convert(req, authorityEntity);
        authorityRepository.save(authorityEntity);
        executeDelAllRoleAuthTask();
    }

    @Override
    public void deleteAuthorities(List<Long> ids) {
        authorityRepository.deleteAllById(ids);
        executeDelAllRoleAuthTask();
    }

    @Override
    public byte[] download() {
        List<AuthorityEntity> authorityEntities = authorityRepository.findAll();
        List<RoleAuthorityEntity> roleAuthorityEntities = roleAuthorityRepository.findAll();

        return SQLUtils.compose(
                SQLUtils.entityToInsertSQL(authorityEntities, Const.AUTHORITY_TABLE),
                SQLUtils.entityToInsertSQL(roleAuthorityEntities, Const.ROLE_AUTHORITY_TABLE))
                .getBytes();
    }

    private void executeDelAllRoleAuthTask() {
        taskExecutor.execute(() -> {
            List<String> allRoleCodes = roleRepository.findAllCodes();
            var authMenuIndexMessage = new AuthMenuIndexMessage(allRoleCodes, AuthMenuOperateEnum.AUTH.getType());
            applicationContext.publishEvent(new AuthMenuOperateEvent(this, authMenuIndexMessage));
        });
    }
}
