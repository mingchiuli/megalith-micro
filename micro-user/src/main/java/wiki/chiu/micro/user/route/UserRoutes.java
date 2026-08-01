package wiki.chiu.micro.user.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.vo.AuthorityRpcVo;
import wiki.chiu.micro.common.vo.MenuRpcVo;
import wiki.chiu.micro.common.vo.RoleEntityRpcVo;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.handler.AuthorityHttpHandler;
import wiki.chiu.micro.user.handler.AuthorityInternalHttpHandler;
import wiki.chiu.micro.user.handler.MenuHttpHandler;
import wiki.chiu.micro.user.handler.MenuInternalHttpHandler;
import wiki.chiu.micro.user.handler.RoleHttpHandler;
import wiki.chiu.micro.user.handler.UserHttpHandler;
import wiki.chiu.micro.user.handler.UserInternalHttpHandler;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.req.MenuEntityReq;
import wiki.chiu.micro.user.req.RoleEntityReq;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.vo.AuthorityVo;
import wiki.chiu.micro.user.vo.MenuAuthorityVo;
import wiki.chiu.micro.user.vo.MenuDisplayVo;
import wiki.chiu.micro.user.vo.MenuEntityVo;
import wiki.chiu.micro.user.vo.RoleEntityVo;
import wiki.chiu.micro.user.vo.RoleMenuVo;
import wiki.chiu.micro.user.vo.UserEntityVo;
import wiki.chiu.micro.common.web.ValidatedRequest;

import java.util.List;
import java.util.function.Function;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.badRequestErrors;
import static wiki.chiu.micro.common.web.FunctionalWeb.multipartFile;
import static wiki.chiu.micro.common.web.FunctionalWeb.nullableParam;
import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.optionalParam;
import static wiki.chiu.micro.common.web.FunctionalWeb.pathVariable;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
        Result.class,
        PageAdapter.class,
        UserEntityRegisterReq.class,
        UserEntityReq.class,
        RoleEntityReq.class,
        MenuEntityReq.class,
        AuthorityEntityReq.class,
        UserEntityVo.class,
        RoleEntityVo.class,
        RoleMenuVo.class,
        MenuEntityVo.class,
        MenuDisplayVo.class,
        MenuAuthorityVo.class,
        AuthorityVo.class,
        UserEntityRpcVo.class,
        RoleEntityRpcVo.class,
        MenuRpcVo.class,
        AuthorityRpcVo.class
})
public class UserRoutes {

    private static final Logger log = LoggerFactory.getLogger(UserRoutes.class);

    private static final ParameterizedTypeReference<List<Long>> LONG_LIST =
            new ParameterizedTypeReference<>() { };

    private static final ParameterizedTypeReference<List<String>> STRING_LIST =
            new ParameterizedTypeReference<>() { };

    @Bean
    RouterFunction<ServerResponse> userRouter(UserHttpHandler userHandler,
                                              RoleHttpHandler roleHandler,
                                              MenuHttpHandler menuHandler,
                                              AuthorityHttpHandler authorityHandler,
                                              UserInternalHttpHandler userInternalHandler,
                                              MenuInternalHttpHandler menuInternalHandler,
                                              AuthorityInternalHttpHandler authorityInternalHandler,
                                              ValidatedRequest validation) {
        return routes(userHandler, roleHandler, menuHandler, authorityHandler,
                userInternalHandler, menuInternalHandler, authorityInternalHandler, validation);
    }

    public static RouterFunction<ServerResponse> routes(UserHttpHandler userHandler,
                                                        RoleHttpHandler roleHandler,
                                                        MenuHttpHandler menuHandler,
                                                        AuthorityHttpHandler authorityHandler,
                                                        UserInternalHttpHandler userInternalHandler,
                                                        MenuInternalHttpHandler menuInternalHandler,
                                                        AuthorityInternalHttpHandler authorityInternalHandler,
                                                        ValidatedRequest validation) {
        return route()
                .GET("/sys/user/auth/register/page", request -> ok(userHandler.getRegisterPage(
                        requiredParam(request, "username"))))
                .GET("/sys/user/register/check", request -> ok(userHandler.checkRegisterPage(
                        nullableParam(request, "token", Function.identity()))))
                .POST("/sys/user/register/save", request -> ok(userHandler.saveRegisterPage(
                        validation.body(request, UserEntityRegisterReq.class))))
                .POST("/sys/user/register/image/upload", request -> ok(userHandler.imageUpload(
                        multipartFile(request, "image"), requiredParam(request, "token"))))
                .GET("/sys/user/register/image/delete", request -> ok(userHandler.imageDelete(
                        requiredParam(request, "url"), requiredParam(request, "token"))))
                .POST("/sys/user/save", request -> ok(userHandler.saveOrUpdate(
                        validation.body(request, UserEntityReq.class))))
                .GET("/sys/user/page/{currentPage}", request -> ok(userHandler.page(
                        pathVariable(request, "currentPage", Integer::valueOf),
                        optionalParam(request, "size", 5, Integer::valueOf))))
                .POST("/sys/user/delete", request -> ok(userHandler.delete(
                        validation.notEmpty(request.body(LONG_LIST), "ids"))))
                .GET("/sys/user/info/{id}", request -> ok(userHandler.info(
                        pathVariable(request, "id", Long::valueOf))))
                .GET("/sys/user/download", request -> userDownload(userHandler))

                .GET("/sys/role/info/{id}", request -> ok(roleHandler.info(
                        pathVariable(request, "id", Long::valueOf))))
                .GET("/sys/role/roles", request -> ok(roleHandler.getPage(
                        optionalParam(request, "currentPage", 1, Integer::valueOf),
                        optionalParam(request, "size", 5, Integer::valueOf))))
                .POST("/sys/role/save", request -> ok(roleHandler.saveOrUpdate(
                        validation.body(request, RoleEntityReq.class))))
                .POST("/sys/role/delete", request -> ok(roleHandler.delete(
                        validation.notEmpty(request.body(LONG_LIST), "ids"))))
                .POST("/sys/role/menu/{roleId}", request -> ok(roleHandler.saveMenu(
                        pathVariable(request, "roleId", Long::valueOf), request.body(LONG_LIST))))
                .GET("/sys/role/menu/{roleId}", request -> ok(roleHandler.getMenusInfo(
                        pathVariable(request, "roleId", Long::valueOf))))
                .GET("/sys/role/download", request -> ok(roleHandler.download()))
                .GET("/sys/role/valid/all", request -> ok(roleHandler.getValidAll()))

                .GET("/sys/menu/info/{id}", request -> ok(menuHandler.info(
                        pathVariable(request, "id", Long::valueOf))))
                .GET("/sys/menu/list", request -> ok(menuHandler.list()))
                .POST("/sys/menu/save", request -> ok(menuHandler.saveOrUpdate(
                        validation.body(request, MenuEntityReq.class))))
                .POST("/sys/menu/delete/{id}", request -> ok(menuHandler.delete(
                        pathVariable(request, "id", Long::valueOf))))
                .GET("/sys/menu/download", request -> ok(menuHandler.download()))
                .POST("/sys/menu/authority/{menuId}", request -> ok(menuHandler.saveAuthority(
                        pathVariable(request, "menuId", Long::valueOf), request.body(LONG_LIST))))
                .GET("/sys/menu/authority/{menuId}", request -> ok(menuHandler.getAuthoritiesInfo(
                        pathVariable(request, "menuId", Long::valueOf))))

                .GET("/sys/authority/list", request -> ok(authorityHandler.list()))
                .GET("/sys/authority/info/{id}", request -> ok(authorityHandler.info(
                        pathVariable(request, "id", Long::valueOf))))
                .POST("/sys/authority/save", request -> ok(authorityHandler.saveOrUpdate(
                        validation.body(request, AuthorityEntityReq.class))))
                .POST("/sys/authority/delete", request -> ok(authorityHandler.delete(
                        validation.notEmpty(request.body(LONG_LIST), "ids"))))
                .GET("/sys/authority/download", request -> ok(authorityHandler.download()))

                .GET("/inner/menu/nav", request -> ok(menuInternalHandler.getCurrentUserNav(
                        requiredParam(request, "role"))))
                .GET("/inner/user/status", request -> ok(userInternalHandler.changeUserStatusByUsername(
                        requiredParam(request, "username"),
                        requiredParam(request, "status", Integer::valueOf))))
                .POST("/inner/user/role", request -> ok(userInternalHandler.findByRoleCodeInAndStatus(
                        request.body(STRING_LIST), requiredParam(request, "status", Integer::valueOf))))
                .POST("/inner/user/login/time", request -> ok(userInternalHandler.updateLoginTime(
                        requiredParam(request, "username"))))
                .GET("/inner/user/email", request -> ok(userInternalHandler.findByEmail(
                        requiredParam(request, "email"))))
                .GET("/inner/user/phone", request -> ok(userInternalHandler.findByPhone(
                        requiredParam(request, "phone"))))
                .GET("/inner/user/role/{userId}", request -> ok(userInternalHandler.findRoleCodesByUserId(
                        pathVariable(request, "userId", Long::valueOf))))
                .GET("/inner/user/login/query", request -> ok(userInternalHandler.findByUsernameOrEmailOrPhone(
                        requiredParam(request, "username"))))
                .GET("/inner/user/{userId}", request -> ok(userInternalHandler.findById(
                        pathVariable(request, "userId", Long::valueOf))))
                .POST("/inner/authority/list", request -> ok(authorityInternalHandler.getAuthorities()))
                .GET("/inner/authority/role", request -> ok(authorityInternalHandler.getAuthoritiesByRoleCode(
                        requiredParam(request, "rawRole"))))
                .filter(badRequestErrors(log))
                .build();
    }

    private static ServerResponse userDownload(UserHttpHandler handler) {
        return ServerResponse.ok().build((request, response) -> {
            handler.download(response);
            return null;
        });
    }
}
