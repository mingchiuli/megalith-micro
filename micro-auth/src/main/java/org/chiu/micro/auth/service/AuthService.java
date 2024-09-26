package org.chiu.micro.auth.service;


import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.req.AuthorityRouteReq;
import org.chiu.micro.auth.vo.AuthorityRouteVo;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;

import java.util.List;

public interface AuthService {

    MenusAndButtonsVo getCurrentUserNav(List<String> role);

    AuthorityRouteVo route(AuthorityRouteReq req, String token);

    AuthDto getAuthDto(String token);
}
