package wiki.chiu.micro.common.rpc;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.vo.MenuRpcVo;

import java.util.List;

public interface MenuHttpService {


    @GetExchange("/menu/nav")
    Result<List<MenuRpcVo>> getCurrentUserNav(@RequestParam String role);
}
