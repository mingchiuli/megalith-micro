package wiki.chiu.micro.search.rpc;

import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.AuthHttpService;
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
