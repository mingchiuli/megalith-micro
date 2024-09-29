package org.chiu.micro.auth.convertor;

import org.chiu.micro.auth.dto.ButtonDto;
import org.chiu.micro.auth.lang.StatusEnum;

import java.util.List;
import java.util.stream.Stream;

/**
 * @Author limingjiu
 * @Date 2024/4/20 18:40
 **/
public class ButtonVoConvertor {

    private ButtonVoConvertor() {
    }

    public static List<ButtonDto> convert(List<ButtonDto> buttons, Boolean statusCheck) {
        Stream<ButtonDto> buttonStream = buttons.stream();
        if (Boolean.TRUE.equals(statusCheck)) {
            buttonStream = buttonStream.filter(menu -> StatusEnum.NORMAL.getCode().equals(menu.status()));
        }

        return buttonStream
                .distinct()
                .toList();
    }
}
