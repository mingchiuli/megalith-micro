package wiki.chiu.micro.user.service;


import wiki.chiu.micro.user.vo.MenusAndButtonsRpcVo;
import wiki.chiu.micro.user.vo.RoleMenuVo;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
public interface RoleMenuService {

    MenusAndButtonsRpcVo getCurrentRoleNav(String role) ;

    List<RoleMenuVo> getMenusInfo(Long roleId);

    void saveMenu(Long roleId, List<Long> menuIds);

    void delete(Long id);

}
