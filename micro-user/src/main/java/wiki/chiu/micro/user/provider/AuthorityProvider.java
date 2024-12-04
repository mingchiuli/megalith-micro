package wiki.chiu.micro.user.provider;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.service.AuthorityService;
import wiki.chiu.micro.user.service.RoleAuthorityService;
import wiki.chiu.micro.user.vo.AuthorityRpcVo;
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

    @GetMapping("/role")
    Result<List<String>> getAuthoritiesByRoleCodes(@RequestParam String rawRole) {
        return Result.success(() -> roleAuthorityService.getAuthoritiesByRoleCodes(rawRole));
    }

}
