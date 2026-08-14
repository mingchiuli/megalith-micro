package wiki.chiu.micro.user.route;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.api.vo.AuthorityRpcVo;
import wiki.chiu.micro.user.api.vo.MenuRpcVo;
import wiki.chiu.micro.user.api.vo.RoleAuthorizationRpcVo;
import wiki.chiu.micro.user.api.vo.RoleEntityRpcVo;
import wiki.chiu.micro.user.api.vo.UserAccessRpcVo;
import wiki.chiu.micro.user.api.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.handler.AuthorityHttpHandler;
import wiki.chiu.micro.user.handler.AuthorityInternalHttpHandler;
import wiki.chiu.micro.user.handler.MenuHttpHandler;
import wiki.chiu.micro.user.handler.MenuInternalHttpHandler;
import wiki.chiu.micro.user.handler.RoleHttpHandler;
import wiki.chiu.micro.user.handler.UserHttpHandler;
import wiki.chiu.micro.user.handler.UserInternalHttpHandler;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.req.MenuEntityReq;
import wiki.chiu.micro.user.req.RegisterImageDeleteReq;
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

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding({
  Result.class,
  PageAdapter.class,
  UserEntityRegisterReq.class,
  UserEntityReq.class,
  RegisterImageDeleteReq.class,
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
  UserAccessRpcVo.class,
  RoleAuthorizationRpcVo.class,
  RoleEntityRpcVo.class,
  MenuRpcVo.class,
  AuthorityRpcVo.class,
  DataPermissionEnum.class
})
public class UserRoutes {

  private static final Logger log = LoggerFactory.getLogger(UserRoutes.class);

  @Bean
  RouterFunction<ServerResponse> userRouter(
      UserHttpHandler userHandler,
      RoleHttpHandler roleHandler,
      MenuHttpHandler menuHandler,
      AuthorityHttpHandler authorityHandler,
      UserInternalHttpHandler userInternalHandler,
      MenuInternalHttpHandler menuInternalHandler,
      AuthorityInternalHttpHandler authorityInternalHandler) {
    return routes(
        userHandler,
        roleHandler,
        menuHandler,
        authorityHandler,
        userInternalHandler,
        menuInternalHandler,
        authorityInternalHandler);
  }

  public static RouterFunction<ServerResponse> routes(
      UserHttpHandler userHandler,
      RoleHttpHandler roleHandler,
      MenuHttpHandler menuHandler,
      AuthorityHttpHandler authorityHandler,
      UserInternalHttpHandler userInternalHandler,
      MenuInternalHttpHandler menuInternalHandler,
      AuthorityInternalHttpHandler authorityInternalHandler) {
    return withDefaultErrorHandling(
            route()
                .GET("/sys/user/auth/register/page", userHandler::getRegisterPage)
                .GET("/sys/user/register/check", userHandler::checkRegisterPage)
                .POST("/sys/user/register/save", userHandler::saveRegisterPage)
                .POST("/sys/user/register/image/upload", userHandler::imageUpload)
                .DELETE("/sys/user/register/image/delete", userHandler::imageDelete)
                .POST("/sys/user/save", userHandler::saveOrUpdate)
                .GET("/sys/user/page/{currentPage}", userHandler::page)
                .POST("/sys/user/delete", userHandler::delete)
                .GET("/sys/user/info/{id}", userHandler::info)
                .GET("/sys/user/download", userHandler::download)
                .GET("/sys/role/info/{id}", roleHandler::info)
                .GET("/sys/role/roles", roleHandler::getPage)
                .POST("/sys/role/save", roleHandler::saveOrUpdate)
                .POST("/sys/role/delete", roleHandler::delete)
                .POST("/sys/role/menu/{roleId}", roleHandler::saveMenu)
                .GET("/sys/role/menu/{roleId}", roleHandler::getMenusInfo)
                .GET("/sys/role/download", roleHandler::download)
                .GET("/sys/role/valid/all", roleHandler::getValidAll)
                .GET("/sys/menu/info/{id}", menuHandler::info)
                .GET("/sys/menu/list", menuHandler::list)
                .POST("/sys/menu/save", menuHandler::saveOrUpdate)
                .POST("/sys/menu/delete/{id}", menuHandler::delete)
                .GET("/sys/menu/download", menuHandler::download)
                .POST("/sys/menu/authority/{menuId}", menuHandler::saveAuthority)
                .GET("/sys/menu/authority/{menuId}", menuHandler::getAuthoritiesInfo)
                .GET("/sys/authority/list", authorityHandler::list)
                .GET("/sys/authority/info/{id}", authorityHandler::info)
                .POST("/sys/authority/save", authorityHandler::saveOrUpdate)
                .POST("/sys/authority/delete", authorityHandler::delete)
                .GET("/sys/authority/download", authorityHandler::download)
                .GET("/inner/menu/nav", menuInternalHandler::getCurrentUserNav)
                .PATCH(
                    "/inner/user/{userId}/password-lock",
                    userInternalHandler::lockAfterPasswordFailures)
                .POST("/inner/user/role", userInternalHandler::findByRoleCodeInAndStatus)
                .POST("/inner/user/login/time", userInternalHandler::updateLoginTime)
                .GET("/inner/user/email", userInternalHandler::findByEmail)
                .GET("/inner/user/phone", userInternalHandler::findByPhone)
                .GET("/inner/user/access/{userId}", userInternalHandler::findUserAccess)
                .GET(
                    "/inner/role/authorization/{roleId}",
                    userInternalHandler::findRoleAuthorization)
                .POST("/inner/role/authorizations", userInternalHandler::findRoleAuthorizations)
                .GET("/inner/user/login/query", userInternalHandler::findByUsernameOrEmailOrPhone)
                .GET("/inner/user/{userId}", userInternalHandler::findById)
                .GET("/inner/authority/list", authorityInternalHandler::getAuthorities),
            log)
        .build();
  }
}
