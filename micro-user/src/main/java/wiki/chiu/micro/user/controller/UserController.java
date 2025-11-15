package wiki.chiu.micro.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.service.UserRoleService;
import wiki.chiu.micro.user.service.UserService;
import wiki.chiu.micro.user.valid.RegisterSave;
import wiki.chiu.micro.user.valid.UserSave;
import wiki.chiu.micro.user.vo.UserEntityVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/sys/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService, UserRoleService userRoleService) {
        this.userService = userService;
    }

    @GetMapping("/auth/register/page")
    public Result<String> getRegisterPage(@RequestParam String username) {
        return Result.success(() -> userService.getRegisterPage(username));
    }

    @GetMapping("/register/check")
    public Result<Boolean> checkRegisterPage(String token) {
        return Result.success(() -> userService.checkRegisterPage(token));
    }


    @PostMapping("/register/save")
    public Result<Void> saveRegisterPage(@RequestBody @RegisterSave UserEntityRegisterReq req) {
        return Result.success(() -> userService.saveRegisterPage(req));
    }

    @PostMapping("/register/image/upload")
    public Result<String> imageUpload(@RequestParam MultipartFile image,
                                      @RequestParam String token) {
        return Result.success(() -> userService.imageUpload(token, image));
    }

    @GetMapping("/register/image/delete")
    public Result<Void> imageDelete(@RequestParam String url,
                                    @RequestParam String token) {
        return Result.success(() -> userService.imageDelete(token, url));
    }

    @PostMapping("/save")
    public Result<Void> saveOrUpdate(@RequestBody @UserSave UserEntityReq userEntityReq) {
        return Result.success(() -> userService.saveOrUpdate(userEntityReq));
    }

    @GetMapping("/page/{currentPage}")
    public Result<PageAdapter<UserEntityVo>> page(@PathVariable Integer currentPage,
                                                  @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(() -> userService.listPage(currentPage, size));
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody @NotEmpty List<Long> ids) {
        return Result.success(() -> userService.deleteUsers(ids));
    }

    @GetMapping("/info/{id}")
    public Result<UserEntityVo> info(@PathVariable Long id) {
        return Result.success(() -> userService.findInfo(id));
    }

    @GetMapping("/download")
    public Result<Void> download(HttpServletResponse response) {
        userService.download(response);
        return Result.success();
    }
}
