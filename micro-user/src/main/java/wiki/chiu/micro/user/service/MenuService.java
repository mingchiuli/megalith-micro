package wiki.chiu.micro.user.service;


import wiki.chiu.micro.user.req.MenuEntityReq;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.MenuEntityVo;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-12-04 2:25 am
 */
public interface MenuService {

    MenuEntityVo findById(Long id);

    void saveOrUpdate(MenuEntityReq menu);

    List<MenuDisplayVo> tree();

    byte[] download();
}
