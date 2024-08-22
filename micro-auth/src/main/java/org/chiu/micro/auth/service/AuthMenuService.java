package org.chiu.micro.auth.service;


import org.chiu.micro.auth.vo.MenusAndButtonsVo;

import java.util.List;

public interface AuthMenuService {

    MenusAndButtonsVo getCurrentUserNav(List<String> role);

}
