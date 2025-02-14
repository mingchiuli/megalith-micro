package wiki.chiu.micro.common.rpc;


import wiki.chiu.micro.common.exception.AuthException;
import wiki.chiu.micro.common.vo.AuthRpcVo;
import wiki.chiu.micro.common.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.AuthorityRouteCheckReq;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import wiki.chiu.micro.common.req.AuthorityRouteReq;


public interface AuthHttpService {

    @GetExchange("/auth")
    Result<AuthRpcVo> getAuthentication(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/auth/route/check")
    Result<Boolean> routeCheck(@RequestBody AuthorityRouteCheckReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/auth/route")
    Result<AuthorityRouteRpcVo> getAuthorityRoute(@RequestBody AuthorityRouteReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);
}
