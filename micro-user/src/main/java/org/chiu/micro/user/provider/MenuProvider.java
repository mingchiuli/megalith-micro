package org.chiu.micro.user.provider;

import org.chiu.micro.user.lang.Result;
import org.chiu.micro.user.service.RoleMenuService;
import org.chiu.micro.user.vo.MenusAndButtonsVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inner/menu")
@Validated
public class MenuProvider {

    private final RoleMenuService roleMenuService;
  
    @PostMapping("/nav")
    public Result<MenusAndButtonsVo> nav(@RequestBody @NotBlank String role) {
        return Result.success(() -> roleMenuService.getCurrentUserNav(role));
    }
}
