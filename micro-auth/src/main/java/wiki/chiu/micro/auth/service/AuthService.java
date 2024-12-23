package wiki.chiu.micro.auth.service;


import wiki.chiu.micro.auth.dto.AuthDto;
import wiki.chiu.micro.auth.vo.AuthorityRouteVo;
import wiki.chiu.micro.auth.vo.MenusAndButtonsVo;
import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import wiki.chiu.micro.common.req.AuthorityRouteReq;

import java.util.List;

public interface AuthService {

    MenusAndButtonsVo getCurrentUserNav(List<String> role);

    AuthorityRouteVo findRoute(AuthorityRouteReq req);

    AuthDto getAuthDto(String token) throws AuthException;

    Boolean routeCheck(AuthorityRouteCheckReq req, String token);
}
