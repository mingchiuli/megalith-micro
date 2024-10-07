package org.chiu.micro.user.service;

import org.chiu.micro.user.req.AuthorityEntityReq;
import org.chiu.micro.user.vo.AuthorityRpcVo;
import org.chiu.micro.user.vo.AuthorityVo;

import java.util.List;

public interface AuthorityService {

    List<AuthorityRpcVo> findAllByService(List<String> service);

    List<AuthorityVo> findAll();

    AuthorityVo findById(Long id);

    void saveOrUpdate(AuthorityEntityReq req);

    void deleteAuthorities(List<Long> ids);

    byte[] download();
}
