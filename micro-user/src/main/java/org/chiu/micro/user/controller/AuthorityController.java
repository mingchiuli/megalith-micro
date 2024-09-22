package org.chiu.micro.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.chiu.micro.user.lang.Result;
import org.chiu.micro.user.req.AuthorityEntityReq;
import org.chiu.micro.user.service.AuthorityService;
import org.chiu.micro.user.vo.AuthorityVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/sys/authority")
@Validated
public class AuthorityController {

    private final AuthorityService authorityService;

    public AuthorityController(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @GetMapping("/list")
    public Result<List<AuthorityVo>> list() {
        return Result.success(authorityService::findAll);
    }

    @GetMapping("/info/{id}")
    public Result<AuthorityVo> info(@PathVariable Long id) {
        return Result.success(() -> authorityService.findById(id));
    }

    @PostMapping("/save")
    public Result<Void> saveOrUpdate(@RequestBody @Valid AuthorityEntityReq req) {
        return Result.success(() -> authorityService.saveOrUpdate(req));
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody @NotEmpty List<Long> ids) {
        return Result.success(() -> authorityService.deleteAuthorities(ids));
    }

    @GetMapping("/download")
    public byte[] download() {
        return authorityService.download();
    }
}
