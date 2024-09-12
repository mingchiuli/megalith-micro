package org.chiu.micro.auth.service;


import org.chiu.micro.auth.req.AuthorityRouteReq;
import org.chiu.micro.auth.vo.AuthorityRouteVo;
import org.chiu.micro.auth.vo.AuthorityVo;
import org.chiu.micro.auth.vo.MenusAndButtonsVo;

import java.util.List;

public interface AuthService {

    MenusAndButtonsVo getCurrentUserNav(List<String> role);

    List<AuthorityVo> getSystemAuthority(List<String> serviceHost);

    AuthorityRouteVo route(AuthorityRouteReq req);

}
