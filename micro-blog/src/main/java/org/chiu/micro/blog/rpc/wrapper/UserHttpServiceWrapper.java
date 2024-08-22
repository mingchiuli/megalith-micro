package org.chiu.micro.blog.rpc.wrapper;


import org.chiu.micro.blog.dto.UserEntityDto;
import org.chiu.micro.blog.exception.MissException;
import org.chiu.micro.blog.lang.Result;
import org.chiu.micro.blog.rpc.UserHttpService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserHttpServiceWrapper {
  
    private final UserHttpService userHttpService;

    public UserEntityDto findById(Long userId) {
        Result<UserEntityDto> result = userHttpService.findById(userId);
        if (result.getCode() != 200) {
            throw new MissException(result.getMsg());
        }
        return result.getData();
    }
}
