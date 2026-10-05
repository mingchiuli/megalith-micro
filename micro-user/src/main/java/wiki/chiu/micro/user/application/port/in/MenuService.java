package wiki.chiu.micro.user.application.port.in;

import java.util.List;

import wiki.chiu.micro.user.application.model.MenuDraft;
import wiki.chiu.micro.user.application.model.MenuExport;
import wiki.chiu.micro.user.application.model.MenuNode;
import wiki.chiu.micro.user.domain.Menu;

public interface MenuService {

    List<Menu> findAll();

    List<MenuNode> tree();

    Menu findById(Long id);

    void saveOrUpdate(MenuDraft menu);

    void delete(Long id);

    MenuExport export();
}
