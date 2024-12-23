package wiki.chiu.micro.common.rpc;


import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.dto.AuthorityRouteRpcDto;
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
    Result<AuthRpcDto> getAuthentication();

    @GetExchange("/auth")
    Result<AuthRpcDto> getAuthentication(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/auth/route/check")
    Result<Boolean> routeCheck(@RequestBody AuthorityRouteCheckReq req, @RequestHeader(value = HttpHeaders.AUTHORIZATION) String token);

    @PostExchange("/auth/route/check")
    Result<Boolean> routeCheck(@RequestBody AuthorityRouteCheckReq req);

    @PostExchange("/auth/route")
    Result<AuthorityRouteRpcDto> getAuthorityRoute(@RequestBody AuthorityRouteReq req);
}
