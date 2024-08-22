package org.chiu.micro.blog.rpc;

import org.chiu.micro.blog.dto.UserEntityDto;
import org.chiu.micro.blog.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface UserHttpService {
  
    @GetExchange("/user/{userId}")
    Result<UserEntityDto> findById(@PathVariable Long userId);
}
