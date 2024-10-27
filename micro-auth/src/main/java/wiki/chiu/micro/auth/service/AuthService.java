package wiki.chiu.micro.auth.service;


import wiki.chiu.micro.auth.dto.AuthDto;
import wiki.chiu.micro.auth.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.vo.AuthorityRouteVo;
import wiki.chiu.micro.auth.vo.MenusAndButtonsVo;
import wiki.chiu.micro.common.exception.AuthException;

import java.util.List;

public interface AuthService {

    MenusAndButtonsVo getCurrentUserNav(List<String> role);

    AuthorityRouteVo route(AuthorityRouteReq req, String token);

    AuthDto getAuthDto(String token) throws AuthException;
}