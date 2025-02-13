package wiki.chiu.micro.user.convertor;

import wiki.chiu.micro.common.vo.ButtonRpcVo;
import wiki.chiu.micro.user.entity.MenuEntity;

import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:40
 **/
public class ButtonRpcVoConvertor {

    private ButtonRpcVoConvertor() {
    }

    public static List<ButtonRpcVo> convert(List<MenuEntity> buttons) {
        return buttons.stream()
                .map(item -> ButtonRpcVo.builder()
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
