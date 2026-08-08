package wiki.chiu.micro.auth.service;

import wiki.chiu.micro.auth.api.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.vo.AuthRpcVo;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;

public interface AuthService {

  MenuWithChildVo getCurrentUserNav(Long userId);

  AuthorityRouteRpcVo findRoute(AuthorityRouteReq req);

  AuthRpcVo getAuthVo(String token);

  Boolean routeCheck(AuthorityRouteCheckReq req, String token);
}
