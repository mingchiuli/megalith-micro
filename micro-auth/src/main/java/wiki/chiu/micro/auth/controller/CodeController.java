package wiki.chiu.micro.auth.controller;

import wiki.chiu.micro.auth.service.CodeService;
import wiki.chiu.micro.common.lang.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author mingchiuli
 * @create 2022-11-27 6:32 pm
 */
@RestController
@RequestMapping("/code")
public class CodeController {

    private final CodeService codeService;

    public CodeController(CodeService codeService) {
        this.codeService = codeService;
    }

    @GetMapping("/email")
    public Result<Void> createEmailCode(@RequestParam(value = "loginName") String loginEmail) {
        return Result.success(() -> codeService.createEmailCode(loginEmail));
    }

    @GetMapping("/sms")
    public Result<Void> createSmsCode(@RequestParam(value = "loginName") String loginSMS) {
        return Result.success(() -> codeService.createSMSCode(loginSMS));
    }
}
