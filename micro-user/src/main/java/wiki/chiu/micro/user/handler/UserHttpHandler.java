package wiki.chiu.micro.user.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.convertor.UserRequestConvertor;
import wiki.chiu.micro.user.req.RegisterImageDeleteReq;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.service.RegistrationService;
import wiki.chiu.micro.user.service.UserAssetService;
import wiki.chiu.micro.user.service.UserExportService;
import wiki.chiu.micro.user.service.UserService;
import wiki.chiu.micro.user.service.UserUpload;

@Component
public class UserHttpHandler {

  private final UserService userService;
  private final RegistrationService registrationService;
  private final UserAssetService assetService;
  private final UserExportService exportService;
  private final ValidatedRequest v;

  private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
      new ParameterizedTypeReference<>() {};

  public UserHttpHandler(
      UserService userService,
      RegistrationService registrationService,
      UserAssetService assetService,
      UserExportService exportService,
      ValidatedRequest v) {
    this.userService = userService;
    this.registrationService = registrationService;
    this.assetService = assetService;
    this.exportService = exportService;
    this.v = v;
  }

  public ServerResponse getRegisterPage(ServerRequest request) {
    String username = requiredParam(request, "username");
    return ok(Result.success(() -> registrationService.issuePage(username)));
  }

  public ServerResponse checkRegisterPage(ServerRequest request) {
    String token = nullableParam(request, "token", Function.identity());
    return ok(Result.success(() -> registrationService.isPageValid(token)));
  }

  public ServerResponse saveRegisterPage(ServerRequest request) throws Exception {
    UserEntityRegisterReq registration = UserRequestConvertor.toUserEntityRegisterReq(request);
    return ok(Result.success(() -> registrationService.register(registration)));
  }

  public ServerResponse imageUpload(ServerRequest request) {
    MultipartFile image = multipartFile(request, "image");
    String token = requiredParam(request, "token");
    try {
      UserUpload upload = new UserUpload(image.getBytes());
      return ok(Result.success(() -> assetService.upload(token, upload)));
    } catch (IOException exception) {
      throw new ValidationException("failed to read uploaded image");
    }
  }

  public ServerResponse imageDelete(ServerRequest request) throws Exception {
    RegisterImageDeleteReq body = request.body(RegisterImageDeleteReq.class);
    String url = v.notBlank(body.url(), "url");
    String token = v.notBlank(body.token(), "token");
    return ok(Result.success(() -> assetService.delete(token, url)));
  }

  public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
    UserEntityReq user = UserRequestConvertor.toUserEntityReq(request);
    return ok(Result.success(() -> userService.saveOrUpdate(user)));
  }

  public ServerResponse page(ServerRequest request) {
    Integer currentPage =
        v.positive(pathVariable(request, "currentPage", Integer::valueOf), "currentPage");
    Integer size = v.positive(optionalParam(request, "size", 5, Integer::valueOf), "size");
    return ok(Result.success(() -> userService.listPage(currentPage, size)));
  }

  public ServerResponse delete(ServerRequest request) throws Exception {
    List<Long> ids = v.notEmpty(v.positiveElements(request.body(LONG_LIST), "ids"), "ids");
    return ok(Result.success(() -> userService.deleteUsers(ids)));
  }

  public ServerResponse info(ServerRequest request) {
    Long id = v.positive(pathVariable(request, "id", Long::valueOf), "id");
    return ok(Result.success(() -> userService.findInfo(id)));
  }

  public ServerResponse download(ServerRequest request) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.sql")
        .build(
            (servletRequest, response) -> {
              exportService.write(response.getOutputStream());
              return null;
            });
  }
}
