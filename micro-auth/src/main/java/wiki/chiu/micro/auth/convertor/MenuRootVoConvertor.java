package wiki.chiu.micro.auth.convertor;

import wiki.chiu.micro.auth.dto.MenuWithChildDto;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;

import java.util.List;

public class MenuRootVoConvertor {

    private MenuRootVoConvertor() {
    }

    public static MenuWithChildVo convert(List<MenuWithChildDto> menuDtos) {
        List<MenuWithChildVo> menuVos = MenuWithChildVoConvertor.convert(menuDtos);
        return !menuVos.isEmpty() ? menuVos.getFirst() : null;
    }
}
