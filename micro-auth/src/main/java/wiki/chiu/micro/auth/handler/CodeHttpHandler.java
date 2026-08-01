package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.auth.service.CodeService;
import wiki.chiu.micro.common.lang.Result;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

@Component
public class CodeHttpHandler {

    private final CodeService codeService;

    public CodeHttpHandler(CodeService codeService) {
        this.codeService = codeService;
    }

    public ServerResponse createEmailCode(ServerRequest request) {
        String loginName = requiredParam(request, "loginName");
        return ok(Result.success(() -> codeService.createEmailCode(loginName)));
    }

    public ServerResponse createSmsCode(ServerRequest request) {
        String loginName = requiredParam(request, "loginName");
        return ok(Result.success(() -> codeService.createSMSCode(loginName)));
    }
}
