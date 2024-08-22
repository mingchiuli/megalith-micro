package org.chiu.micro.exhibit.rpc;

import org.chiu.micro.exhibit.dto.UserEntityDto;
import org.chiu.micro.exhibit.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface UserHttpService {

    @GetExchange("/user/{userId}")
    Result<UserEntityDto> findById(@PathVariable Long userId);
}
