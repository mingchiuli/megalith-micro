package org.chiu.micro.auth.provider;

import org.chiu.micro.auth.dto.AuthDto;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.utils.SecurityAuthenticationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RestController
@RequestMapping(value = "/inner")
@RequiredArgsConstructor
@Validated
public class AuthProvider {

    private final SecurityAuthenticationUtils securityAuthenticationUtils;

    @GetMapping("/auth")
    @SneakyThrows
    public Result<AuthDto> findById(@RequestParam String token) {
        AuthDto authDto = securityAuthenticationUtils.getAuthDto(token);
        return Result.success(authDto);
    }

}
