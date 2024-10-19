package org.chiu.micro.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chiu.micro.common.exception.MissException;
import org.chiu.micro.common.lang.AuthMenuOperateEnum;
import org.chiu.micro.common.lang.StatusEnum;
import org.chiu.micro.user.constant.AuthMenuIndexMessage;
import org.chiu.micro.user.convertor.AuthorityEntityConvertor;
import org.chiu.micro.user.convertor.AuthorityRpcVoConvertor;
import org.chiu.micro.user.convertor.AuthorityVoConvertor;
import org.chiu.micro.user.entity.AuthorityEntity;
import org.chiu.micro.user.event.AuthMenuOperateEvent;
import org.chiu.micro.user.repository.AuthorityRepository;
import org.chiu.micro.user.repository.RoleRepository;
import org.chiu.micro.user.req.AuthorityEntityReq;
import org.chiu.micro.user.service.AuthorityService;
import org.chiu.micro.user.vo.AuthorityRpcVo;
import org.chiu.micro.user.vo.AuthorityVo;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;


@Service
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    private final ObjectMapper objectMapper;

    private final RoleRepository roleRepository;

    private final ApplicationContext applicationContext;

    public AuthorityServiceImpl(AuthorityRepository authorityRepository, ObjectMapper objectMapper, RoleRepository roleRepository, ApplicationContext applicationContext) {
        this.authorityRepository = authorityRepository;
        this.objectMapper = objectMapper;
        this.roleRepository = roleRepository;
        this.applicationContext = applicationContext;
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
        List<AuthorityEntity> authorities = authorityRepository.findAll();
        try {
            return objectMapper.writeValueAsBytes(authorities);
        } catch (JsonProcessingException e) {
            throw new MissException(e.getMessage());
        }
    }
}
