package org.chiu.micro.websocket.rpc.wrapper;

import java.util.Collections;
import java.util.List;

import org.chiu.micro.websocket.dto.AuthorityDto;
import org.chiu.micro.websocket.exception.MissException;
import org.chiu.micro.websocket.lang.Const;
import org.chiu.micro.websocket.lang.Result;
import org.chiu.micro.websocket.rpc.UserHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserHttpServiceWrapper {
  
    private final UserHttpService userHttpService;

     public List<AuthorityDto> getAuthorities() {
        Result<List<AuthorityDto>> result = userHttpService.getAuthorities(Collections.singletonList(Const.WEBSOCKET_SERVICE.getInfo()));
        
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }
}
