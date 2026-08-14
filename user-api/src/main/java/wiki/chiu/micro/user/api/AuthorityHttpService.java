package wiki.chiu.micro.user.api;

import java.util.List;
import org.springframework.web.service.annotation.GetExchange;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;

public interface AuthorityHttpService {

  @GetExchange("/authority/list")
  Result<List<AuthorityRpcVo>> getAuthorities();
}
