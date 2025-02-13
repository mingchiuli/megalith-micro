package wiki.chiu.micro.common.rpc;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;

import java.util.List;
import java.util.Set;

public interface AuthorityHttpService {

    @GetExchange("/authority/role")
    Result<Set<String>> getAuthoritiesByRoleCode(@RequestParam String rawRole);

    @PostExchange("/authority/list")
    Result<List<AuthorityRpcVo>> getAuthorities(@RequestBody List<String> service);
}
