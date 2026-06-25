package wiki.chiu.micro.auth.service;


import wiki.chiu.micro.auth.vo.MenuWithChildVo;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;

import java.util.List;

public interface AuthService {

    MenuWithChildVo getCurrentUserNav(List<String> role);

    AuthorityRouteRpcVo findRoute(AuthorityRouteReq req);

    AuthRpcVo getAuthVo(String token);

    Boolean routeCheck(AuthorityRouteCheckReq req, String token);
}
