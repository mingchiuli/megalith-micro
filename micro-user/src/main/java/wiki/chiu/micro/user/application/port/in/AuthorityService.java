package wiki.chiu.micro.user.application.port.in;

import java.util.List;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.vo.AuthorityVo;

public interface AuthorityService {

  List<AuthorityRpcVo> findAllByService();

  List<AuthorityVo> findAll();

  AuthorityVo findById(Long id);

  void saveOrUpdate(AuthorityEntityReq req);

  void deleteAuthorities(List<Long> ids);

  byte[] download();
}
