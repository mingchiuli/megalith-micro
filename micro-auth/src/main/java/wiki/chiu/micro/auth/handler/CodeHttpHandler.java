package wiki.chiu.micro.auth.handler;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.auth.service.CodeService;
import wiki.chiu.micro.common.lang.Result;

@Component
public class CodeHttpHandler {

    private final CodeService codeService;

    public CodeHttpHandler(CodeService codeService) {
        this.codeService = codeService;
    }

    public Result<Void> createEmailCode(String loginEmail) {
        return Result.success(() -> codeService.createEmailCode(loginEmail));
    }

    public Result<Void> createSmsCode(String loginSms) {
        return Result.success(() -> codeService.createSMSCode(loginSms));
    }
}
