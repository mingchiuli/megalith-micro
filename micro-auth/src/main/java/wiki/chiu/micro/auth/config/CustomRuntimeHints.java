package wiki.chiu.micro.auth.config;

import wiki.chiu.micro.auth.dto.ButtonDto;
import wiki.chiu.micro.auth.dto.MenuDto;
import wiki.chiu.micro.auth.dto.MenusAndButtonsDto;
import wiki.chiu.micro.auth.vo.LoginSuccessVo;
import wiki.chiu.micro.auth.vo.UserInfoVo;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import wiki.chiu.micro.common.dto.AuthorityRpcDto;


public class CustomRuntimeHints implements RuntimeHintsRegistrar {


    @Override// Register method for reflection
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register method for reflection

        hints.serialization()
                .registerType(LoginSuccessVo.class)
                .registerType(UserInfoVo.class)
                .registerType(MenusAndButtonsDto.class)
                .registerType(AuthorityRpcDto.class)
                .registerType(MenuDto.class)
                .registerType(ButtonDto.class);
    }
}
