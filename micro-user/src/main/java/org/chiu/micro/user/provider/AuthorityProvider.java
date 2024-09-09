package org.chiu.micro.user.provider;

import java.util.List;

import org.chiu.micro.user.lang.Result;
import org.chiu.micro.user.service.AuthorityService;
import org.chiu.micro.user.vo.AuthorityVo;
import org.chiu.micro.user.service.RoleAuthorityService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/inner/authority")
@Validated
public class AuthorityProvider {

    private final AuthorityService authorityService;
    
    private final RoleAuthorityService roleAuthorityService;

    @PostMapping("/list")
    public Result<List<AuthorityVo>> list(@RequestBody List<String> service) {
        return Result.success(() -> authorityService.findAllByService(service));
    }

    @GetMapping("/role/{rawRoles}")
    Result<List<String>> getAuthoritiesByRoleCodes(@PathVariable String rawRoles) {
        return Result.success(() -> roleAuthorityService.getAuthoritiesByRoleCodes(rawRoles));
    }

}
