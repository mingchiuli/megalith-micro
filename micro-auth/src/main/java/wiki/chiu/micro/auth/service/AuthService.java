package wiki.chiu.micro.auth.service;


import wiki.chiu.micro.auth.vo.MenusAndButtonsVo;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;

import java.util.List;

public interface AuthService {

    MenusAndButtonsVo getCurrentUserNav(List<String> role);

    AuthorityRouteRpcVo findRoute(AuthorityRouteReq req);

    AuthRpcVo getAuthVo(String token);

    Boolean routeCheck(AuthorityRouteCheckReq req, String token);
}
