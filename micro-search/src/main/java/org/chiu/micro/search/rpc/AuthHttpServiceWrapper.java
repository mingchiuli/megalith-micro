package org.chiu.micro.search.rpc;

import org.chiu.micro.common.dto.AuthRpcDto;
import org.chiu.micro.common.exception.MissException;
import org.chiu.micro.common.lang.Result;
import org.chiu.micro.common.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthRpcDto getAuthentication() {
        Result<AuthRpcDto> result = authHttpService.getAuthentication();
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
