package wiki.chiu.micro.auth.adapter.in.http;

import java.util.List;

import wiki.chiu.micro.auth.api.vo.AuthorityRouteRpcVo;
import wiki.chiu.micro.auth.application.model.MenuDisplay;
import wiki.chiu.micro.auth.application.model.RouteDecision;
import wiki.chiu.micro.auth.application.model.UserInfo;

/**
 * Renders the auth use-case results as the HTTP and RPC payloads the callers expect.
 */
public final class AuthResponseMapper {

    private AuthResponseMapper() {
    }

    /**
     * The navigation response carries the user's first root menu, or null when the user has none.
     */
    public static MenuWithChildVo toNav(List<MenuDisplay> roots) {
        return roots.isEmpty() ? null : toVo(roots.getFirst());
    }

    public static MenuWithChildVo toVo(MenuDisplay menu) {
        List<MenuWithChildVo> children = menu.children().stream().map(AuthResponseMapper::toVo).toList();
        return new MenuWithChildVo(
            menu.id(),
            menu.parentId(),
            menu.title(),
            menu.name(),
            menu.url(),
            menu.component(),
            menu.type(),
            menu.icon(),
            menu.orderNum(),
            menu.status(),
            children);
    }

    public static UserInfoVo toVo(UserInfo info) {
        return new UserInfoVo(info.id(), info.nickname(), info.avatar());
    }

    public static AuthorityRouteRpcVo toRpc(RouteDecision decision) {
        return new AuthorityRouteRpcVo(
            decision.serviceHost(), decision.servicePort(), decision.principal());
    }
}
