package wiki.chiu.micro.user.service;

import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.vo.AuthorityVo;

import java.util.List;

public interface AuthorityService {

    List<AuthorityRpcVo> findAllByService();

    List<AuthorityVo> findAll();

    AuthorityVo findById(Long id);

    void saveOrUpdate(AuthorityEntityReq req);

    void deleteAuthorities(List<Long> ids);

    byte[] download();
}
