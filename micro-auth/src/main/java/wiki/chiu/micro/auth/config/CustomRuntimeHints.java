package wiki.chiu.micro.auth.config;

import wiki.chiu.micro.auth.dto.ButtonDto;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.auth.dto.MenusAndButtonsDto;
import wiki.chiu.micro.auth.vo.LoginSuccessVo;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import org.springframework.aot.hint.*;

import wiki.chiu.micro.common.vo.AuthorityRpcVo;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {


    @Override// Register method for reflection
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection

        hints.serialization()
                .registerType(LoginSuccessVo.class)
                .registerType(UserInfoVo.class)
                .registerType(MenusAndButtonsDto.class)
                .registerType(AuthorityRpcVo.class)
                .registerType(MenuDto.class)
                .registerType(ButtonDto.class);

        hints.resources()
                .registerPattern("logback-spring.xml");
    }
}
