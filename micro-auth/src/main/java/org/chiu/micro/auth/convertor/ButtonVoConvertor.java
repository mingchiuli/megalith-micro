package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.ButtonDto;
import org.chiu.micro.auth.lang.StatusEnum;

import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:40
 **/
public class ButtonVoConvertor {

    private ButtonVoConvertor() {
    }

    public static List<ButtonDto> convert(List<ButtonDto> buttons) {

        return buttons.stream()
                .filter(menu -> StatusEnum.NORMAL.getCode().equals(menu.status()))
                .distinct()
                .toList();
    }
}
