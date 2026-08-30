package wiki.chiu.micro.auth.application.port.in;

import java.util.List;
import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.vo.MenuWithChildVo;

public interface AuthService {

  MenuWithChildVo getCurrentUserNav(List<String> roles);

  AuthorityRouteRpcVo authorizeRoute(AuthorityRouteReq req, String token);
}
