package wiki.chiu.micro.user.handler;


import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.req.MenuEntityReq;
import wiki.chiu.micro.user.service.MenuAuthorityService;
import wiki.chiu.micro.user.service.MenuService;
import org.springframework.stereotype.Component;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.util.List;

import static wiki.chiu.micro.common.web.FunctionalWeb.*;


/**
 * @author mingchiuli
 * @create 2022-12-04 2:22 am
 */
@Component
public class MenuHttpHandler {

    private final MenuService menuService;

    private final MenuAuthorityService menuAuthorityService;
    private final ValidatedRequest validation;

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    public MenuHttpHandler(MenuService menuService, MenuAuthorityService menuAuthorityService,
                           ValidatedRequest validation) {
        this.menuService = menuService;
        this.menuAuthorityService = menuAuthorityService;
        this.validation = validation;
    }

    public ServerResponse info(ServerRequest request) {
        Long id = pathVariable(request, "id", Long::valueOf);
        return ok(Result.success(() -> menuService.findById(id)));
    }

    public ServerResponse list(ServerRequest request) {
        return ok(Result.success(menuService::tree));
    }

    public ServerResponse saveOrUpdate(ServerRequest request) throws Exception {
        MenuEntityReq menu = validation.body(request, MenuEntityReq.class);
        return ok(Result.success(() -> menuService.saveOrUpdate(menu)));
    }

    public ServerResponse delete(ServerRequest request) {
        Long id = pathVariable(request, "id", Long::valueOf);
        return ok(Result.success(() -> menuService.delete(id)));
    }

    public ServerResponse download(ServerRequest request) {
        return ok(menuService.download());
    }

    public ServerResponse saveAuthority(ServerRequest request) throws Exception {
        Long menuId = pathVariable(request, "menuId", Long::valueOf);
        List<Long> authorityIds = validation.body(request, LONG_LIST);
        return ok(Result.success(() -> menuAuthorityService.saveAuthority(menuId, authorityIds)));
    }

    public ServerResponse getAuthoritiesInfo(ServerRequest request) {
        Long menuId = pathVariable(request, "menuId", Long::valueOf);
        return ok(Result.success(() -> menuAuthorityService.getAuthoritiesInfo(menuId)));
    }
}
