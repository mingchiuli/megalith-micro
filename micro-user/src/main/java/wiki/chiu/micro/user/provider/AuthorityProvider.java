package wiki.chiu.micro.user.provider;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.AuthorityHttpService;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.service.AuthorityService;
import wiki.chiu.micro.user.service.RoleAuthorityService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/inner/authority")
@Validated
public class AuthorityProvider implements AuthorityHttpService {

    private final AuthorityService authorityService;

    private final RoleAuthorityService roleAuthorityService;

    public AuthorityProvider(AuthorityService authorityService, RoleAuthorityService roleAuthorityService) {
        this.authorityService = authorityService;
        this.roleAuthorityService = roleAuthorityService;
    }

    @PostMapping("/list")
    public Result<List<AuthorityRpcVo>> getAuthorities(@RequestBody List<String> service) {
        return Result.success(() -> authorityService.findAllByService(service));
    }

    @GetMapping("/role")
    public Result<Set<String>> getAuthoritiesByRoleCode(@RequestParam String rawRole) {
        return Result.success(() -> roleAuthorityService.getAuthoritiesByRoleCodes(rawRole));
    }

}
