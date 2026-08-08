package wiki.chiu.micro.user.handler;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.web.ValidatedRequest;
import wiki.chiu.micro.user.converter.UserRequestConverter;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.service.AuthorityService;

@Component
public class AuthorityHttpHandler {

  private final AuthorityService authorityService;
  private final ValidatedRequest v;

  private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
      new ParameterizedTypeReference<>() {};

  public AuthorityHttpHandler(AuthorityService authorityService, ValidatedRequest v) {
    this.authorityService = authorityService;
    this.v = v;
  }

  public ServerResponse list(ServerRequest request) {
    return ok(Result.success(authorityService::findAll));
  }

  public ServerResponse info(ServerRequest request) {
    Long id = v.positive(pathVariable(request, "id", Long::valueOf), "id");
    return ok(Result.success(() -> authorityService.findById(id)));
  }

  public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
    AuthorityEntityReq authority = UserRequestConverter.toAuthorityEntityReq(request);
    return ok(Result.success(() -> authorityService.saveOrUpdate(authority)));
  }

  public ServerResponse delete(ServerRequest request) throws Exception {
    List<Long> ids = v.notEmpty(v.positiveElements(request.body(LONG_LIST), "ids"), "ids");
    return ok(Result.success(() -> authorityService.deleteAuthorities(ids)));
  }

  public ServerResponse download(ServerRequest request) {
    return ok(authorityService.download());
  }
}
