package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.user.entity.MenuEntity;
import wiki.chiu.micro.user.vo.ButtonVo;

import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:40
 **/
public class ButtonVoConvertor {

    private ButtonVoConvertor() {
    }

    public static List<ButtonVo> convert(List<MenuEntity> buttons) {
        return buttons.stream()
                .map(item -> ButtonVo.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .icon(item.getIcon())
                        .component(item.getComponent())
                        .orderNum(item.getOrderNum())
                        .status(item.getStatus())
                        .parentId(item.getParentId())
                        .url(item.getUrl())
                        .title(item.getTitle())
                        .type(item.getType())
                        .build())
                .toList();
    }
}
