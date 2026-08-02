package wiki.chiu.micro.user.handler;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.function.Function;
import wiki.chiu.micro.common.web.ValidatedRequest;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;

@Component
public class UserHttpHandler {

    private final UserService userService;
    private final ValidatedRequest validation;

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    public UserHttpHandler(UserService userService, ValidatedRequest validation) {
        this.userService = userService;
        this.validation = validation;
    }

    public ServerResponse getRegisterPage(ServerRequest request) {
        String username = requiredParam(request, "username");
        return ok(Result.success(() -> userService.getRegisterPage(username)));
    }

    public ServerResponse checkRegisterPage(ServerRequest request) {
        String token = nullableParam(request, "token", Function.identity());
        return ok(Result.success(() -> userService.checkRegisterPage(token)));
    }

    public ServerResponse saveRegisterPage(ServerRequest request) throws Exception {
        UserEntityRegisterReq registration = validation.body(request, UserEntityRegisterReq.class);
        return ok(Result.success(() -> userService.saveRegisterPage(registration)));
    }

    public ServerResponse imageUpload(ServerRequest request) {
        MultipartFile image = multipartFile(request, "image");
        String token = requiredParam(request, "token");
        return ok(Result.success(() -> userService.imageUpload(token, image)));
    }

    public ServerResponse imageDelete(ServerRequest request) {
        String url = requiredParam(request, "url");
        String token = requiredParam(request, "token");
        return ok(Result.success(() -> userService.imageDelete(token, url)));
    }

    public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
        UserEntityReq user = validation.body(request, UserEntityReq.class);
        return ok(Result.success(() -> userService.saveOrUpdate(user)));
    }

    public ServerResponse page(ServerRequest request) {
        Integer currentPage = positive(pathVariable(request, "currentPage", Integer::valueOf), "currentPage");
        Integer size = positive(optionalParam(request, "size", 5, Integer::valueOf), "size");
        return ok(Result.success(() -> userService.listPage(currentPage, size)));
    }

    public ServerResponse delete(ServerRequest request) throws Exception {
        List<Long> ids = validation.notEmpty(
                validation.positiveElements(validation.body(request, LONG_LIST), "ids"), "ids");
        return ok(Result.success(() -> userService.deleteUsers(ids)));
    }

    public ServerResponse info(ServerRequest request) {
        Long id = positive(pathVariable(request, "id", Long::valueOf), "id");
        return ok(Result.success(() -> userService.findInfo(id)));
    }

    public ServerResponse download(ServerRequest request) {
        return ServerResponse.ok().build((servletRequest, response) -> {
            userService.download(response);
            return null;
        });
    }
}
