package wiki.chiu.micro.search.rpc;

import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import org.springframework.stereotype.Component;

import static wiki.chiu.micro.common.lang.Result.handleResult;

@Component
public class AuthHttpServiceWrapper {

    private final AuthHttpService authHttpService;

    public AuthHttpServiceWrapper(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    public AuthRpcDto getAuthentication() {
        return handleResult(authHttpService::getAuthentication);
    }

}
