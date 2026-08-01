package wiki.chiu.micro.user.handler;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.AuthorityHttpService;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.service.AuthorityService;
import wiki.chiu.micro.user.service.RoleAuthorityService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Set;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;


@Component
public class AuthorityInternalHttpHandler implements AuthorityHttpService {

    private final AuthorityService authorityService;

    private final RoleAuthorityService roleAuthorityService;

    public AuthorityInternalHttpHandler(AuthorityService authorityService, RoleAuthorityService roleAuthorityService) {
        this.authorityService = authorityService;
        this.roleAuthorityService = roleAuthorityService;
    }

    public ServerResponse getAuthorities(ServerRequest request) {
        return ok(getAuthorities());
    }

    public ServerResponse getAuthoritiesByRoleCode(ServerRequest request) {
        return ok(getAuthoritiesByRoleCode(requiredParam(request, "rawRole")));
    }

    @Override
    public Result<List<AuthorityRpcVo>> getAuthorities() {
        return Result.success(() -> authorityService.findAllByService());
    }

    @Override
    public Result<Set<String>> getAuthoritiesByRoleCode(String rawRole) {
        return Result.success(() -> roleAuthorityService.getAuthoritiesByRoleCodes(rawRole));
    }

}
