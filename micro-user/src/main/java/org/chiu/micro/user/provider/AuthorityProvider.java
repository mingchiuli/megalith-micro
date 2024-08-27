package org.chiu.micro.user.provider;

import java.util.List;

import org.chiu.micro.user.lang.Result;
import org.chiu.micro.user.service.AuthorityService;
import org.chiu.micro.user.vo.AuthorityVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/inner/authority")
@Validated
public class AuthorityProvider {

    private final AuthorityService authorityService;

    @GetMapping("/list")
    public Result<List<AuthorityVo>> list() {
        return Result.success(authorityService::findAll);
    }

}
