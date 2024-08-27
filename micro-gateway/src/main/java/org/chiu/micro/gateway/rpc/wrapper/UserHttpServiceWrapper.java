package org.chiu.micro.gateway.rpc.wrapper;

import java.util.List;

import org.chiu.micro.gateway.dto.AuthorityDto;
import org.chiu.micro.gateway.exception.MissException;
import org.chiu.micro.gateway.lang.Const;
import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.rpc.UserHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserHttpServiceWrapper {
  
    private final UserHttpService userHttpService;

     public List<AuthorityDto> getAuthorities() {
        Result<List<AuthorityDto>> result = userHttpService.getAuthorities(Const.SERVICE.getInfo());
        
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }
}