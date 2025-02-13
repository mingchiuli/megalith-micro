package wiki.chiu.micro.common.rpc;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.vo.MenusAndButtonsRpcVo;

public interface MenuHttpService {


    @GetExchange("/menu/nav")
    Result<MenusAndButtonsRpcVo> getCurrentUserNav(@RequestParam String role);
}
