package wiki.chiu.micro.user.handler;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.service.AuthorityService;
import org.springframework.stereotype.Component;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.util.List;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;

@Component
public class AuthorityHttpHandler {

    private final AuthorityService authorityService;
    private final ValidatedRequest validation;

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    public AuthorityHttpHandler(AuthorityService authorityService, ValidatedRequest validation) {
        this.authorityService = authorityService;
        this.validation = validation;
    }

    public ServerResponse list(ServerRequest request) {
        return ok(Result.success(authorityService::findAll));
    }

    public ServerResponse info(ServerRequest request) {
        Long id = pathVariable(request, "id", Long::valueOf);
        return ok(Result.success(() -> authorityService.findById(id)));
    }

    public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
        AuthorityEntityReq authority = validation.body(request, AuthorityEntityReq.class);
        return ok(Result.success(() -> authorityService.saveOrUpdate(authority)));
    }

    public ServerResponse delete(ServerRequest request) throws Exception {
        List<Long> ids = validation.notEmpty(validation.body(request, LONG_LIST), "ids");
        return ok(Result.success(() -> authorityService.deleteAuthorities(ids)));
    }

    public ServerResponse download(ServerRequest request) {
        return ok(authorityService.download());
    }
}
