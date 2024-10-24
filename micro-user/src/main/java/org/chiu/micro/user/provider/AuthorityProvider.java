package org.chiu.micro.user.provider;

import org.chiu.micro.common.lang.Result;
import org.chiu.micro.user.service.AuthorityService;
import org.chiu.micro.user.service.RoleAuthorityService;
import org.chiu.micro.user.vo.AuthorityRpcVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/inner/authority")
@Validated
public class AuthorityProvider {

    private final AuthorityService authorityService;

    private final RoleAuthorityService roleAuthorityService;

    public AuthorityProvider(AuthorityService authorityService, RoleAuthorityService roleAuthorityService) {
        this.authorityService = authorityService;
        this.roleAuthorityService = roleAuthorityService;
    }

    @PostMapping("/list")
    public Result<List<AuthorityRpcVo>> list(@RequestBody List<String> service) {
        return Result.success(() -> authorityService.findAllByService(service));
    }

    @GetMapping("/role/{rawRoles}")
    Result<List<String>> getAuthoritiesByRoleCodes(@PathVariable String rawRoles) {
        return Result.success(() -> roleAuthorityService.getAuthoritiesByRoleCodes(rawRoles));
    }

}
