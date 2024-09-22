package org.chiu.micro.exhibit.rpc.wrapper;

import org.chiu.micro.exhibit.dto.UserEntityDto;
import org.chiu.micro.exhibit.exception.MissException;
import org.chiu.micro.exhibit.lang.Result;
import org.chiu.micro.exhibit.rpc.UserHttpService;
import org.springframework.stereotype.Component;


@Component
public class UserHttpServiceWrapper {

    private final UserHttpService userHttpService;

    public UserHttpServiceWrapper(UserHttpService userHttpService) {
        this.userHttpService = userHttpService;
    }

    public UserEntityDto findById(Long userId) {
        Result<UserEntityDto> result = userHttpService.findById(userId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }
}
