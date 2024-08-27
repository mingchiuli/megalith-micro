package org.chiu.micro.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

import org.chiu.micro.user.lang.Result;
import org.chiu.micro.user.req.AuthorityEntityReq;
import org.chiu.micro.user.service.AuthorityService;
import org.chiu.micro.user.vo.AuthorityVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/sys/authority")
@RequiredArgsConstructor
@Validated
public class AuthorityController {

    private final AuthorityService authorityService;

    @GetMapping("/list")
    public Result<List<AuthorityVo>> list(@RequestParam String service) {
        return Result.success(() -> authorityService.findAll(service));
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
