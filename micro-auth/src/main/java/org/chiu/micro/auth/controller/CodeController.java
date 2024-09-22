package org.chiu.micro.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.chiu.micro.auth.lang.Result;
import org.chiu.micro.auth.service.CodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
    public Result<Void> createSmsCode(@RequestParam(value = "loginName") String loginSMS) throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        codeService.createSMSCode(loginSMS);
        return Result.success();
    }
}
