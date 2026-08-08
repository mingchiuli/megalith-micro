package wiki.chiu.micro.user.service;

import java.util.List;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.vo.RoleMenuVo;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
public interface RoleMenuService {

  List<MenuRpcVo> getCurrentRoleNav(String role);

  List<RoleMenuVo> getMenusInfo(Long roleId);

  void saveMenu(Long roleId, List<Long> menuIds);
}
