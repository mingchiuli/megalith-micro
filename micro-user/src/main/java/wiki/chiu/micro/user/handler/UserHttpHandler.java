package wiki.chiu.micro.user.handler;

import jakarta.servlet.http.HttpServletResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.service.UserRoleService;
import wiki.chiu.micro.user.service.UserService;
import wiki.chiu.micro.user.vo.UserEntityVo;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class UserHttpHandler {

    private final UserService userService;

    public UserHttpHandler(UserService userService, UserRoleService userRoleService) {
        this.userService = userService;
    }

    public Result<String> getRegisterPage(String username) {
        return Result.success(() -> userService.getRegisterPage(username));
    }

    public Result<Boolean> checkRegisterPage(String token) {
        return Result.success(() -> userService.checkRegisterPage(token));
    }


    public Result<Void> saveRegisterPage(UserEntityRegisterReq req) {
        return Result.success(() -> userService.saveRegisterPage(req));
    }

    public Result<String> imageUpload(MultipartFile image, String token) {
        return Result.success(() -> userService.imageUpload(token, image));
    }

    public Result<Void> imageDelete(String url, String token) {
        return Result.success(() -> userService.imageDelete(token, url));
    }

    public Result<Void> saveOrUpdate(UserEntityReq userEntityReq) {
        return Result.success(() -> userService.saveOrUpdate(userEntityReq));
    }

    public Result<PageAdapter<UserEntityVo>> page(Integer currentPage, Integer size) {
        return Result.success(() -> userService.listPage(currentPage, size));
    }

    public Result<Void> delete(List<Long> ids) {
        return Result.success(() -> userService.deleteUsers(ids));
    }

    public Result<UserEntityVo> info(Long id) {
        return Result.success(() -> userService.findInfo(id));
    }

    public Result<Void> download(HttpServletResponse response) {
        userService.download(response);
        return Result.success();
    }
}
