package wiki.chiu.micro.auth.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.auth.application.port.in.CodeService;
import wiki.chiu.micro.common.result.Result;

@Component
public class CodeHttpHandler {

    private final CodeService codeService;

    public CodeHttpHandler(CodeService codeService) {
        this.codeService = codeService;
    }

    public ServerResponse createEmailCode(ServerRequest request) throws Exception {
        String loginName = AuthRequestConvertor.loginName(request);
        return ok(Result.success(() -> codeService.createEmailCode(loginName)));
    }

    public ServerResponse createSmsCode(ServerRequest request) throws Exception {
        String loginName = AuthRequestConvertor.loginName(request);
        return ok(Result.success(() -> codeService.createSMSCode(loginName)));
    }

}
