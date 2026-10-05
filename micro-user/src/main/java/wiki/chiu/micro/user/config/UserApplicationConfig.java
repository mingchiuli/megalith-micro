package wiki.chiu.micro.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.user.application.port.out.PasswordHasher;

import wiki.chiu.micro.user.application.port.in.AuthorityService;
import wiki.chiu.micro.user.application.port.in.MenuAuthorityService;
import wiki.chiu.micro.user.application.port.in.MenuService;
import wiki.chiu.micro.user.application.port.in.RegistrationService;
import wiki.chiu.micro.user.application.port.in.RoleDataPermissionService;
import wiki.chiu.micro.user.application.port.in.RoleMenuService;
import wiki.chiu.micro.user.application.port.in.RoleService;
import wiki.chiu.micro.user.application.port.in.UserAssetService;
import wiki.chiu.micro.user.application.port.in.UserExportService;
import wiki.chiu.micro.user.application.port.in.UserIdentityService;
import wiki.chiu.micro.user.application.port.in.UserRoleService;
import wiki.chiu.micro.user.application.port.in.UserService;
import wiki.chiu.micro.user.application.port.out.AuthorityReader;
import wiki.chiu.micro.user.application.port.out.AuthorityWriter;
import wiki.chiu.micro.user.application.port.out.MenuAuthorityReader;
import wiki.chiu.micro.user.application.port.out.MenuReader;
import wiki.chiu.micro.user.application.port.out.MenuWriter;
import wiki.chiu.micro.user.application.port.out.RegistrationTokenStore;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionReader;
import wiki.chiu.micro.user.application.port.out.RoleDataPermissionWriter;
import wiki.chiu.micro.user.application.port.out.RoleMenuReader;
import wiki.chiu.micro.user.application.port.out.RoleMenuWriter;
import wiki.chiu.micro.user.application.port.out.RoleReader;
import wiki.chiu.micro.user.application.port.out.RoleWriter;
import wiki.chiu.micro.user.application.port.out.UserAssetStorage;
import wiki.chiu.micro.user.application.port.out.UserIdentityWriter;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.application.port.out.UserRoleReader;
import wiki.chiu.micro.user.application.port.out.UserWriter;
import wiki.chiu.micro.user.application.service.AuthorityServiceImpl;
import wiki.chiu.micro.user.application.service.AuthorizationQueryService;
import wiki.chiu.micro.user.application.service.MenuAuthorityServiceImpl;
import wiki.chiu.micro.user.application.service.MenuServiceImpl;
import wiki.chiu.micro.user.application.service.RegistrationServiceImpl;
import wiki.chiu.micro.user.application.service.RoleDataPermissionServiceImpl;
import wiki.chiu.micro.user.application.service.RoleMenuServiceImpl;
import wiki.chiu.micro.user.application.service.RoleServiceImpl;
import wiki.chiu.micro.user.application.service.UserAssetServiceImpl;
import wiki.chiu.micro.user.application.service.UserExportServiceImpl;
import wiki.chiu.micro.user.application.service.UserIdentityServiceImpl;
import wiki.chiu.micro.user.application.service.UserRoleServiceImpl;
import wiki.chiu.micro.user.application.service.UserServiceImpl;

/**
 * Wires the use cases to the adapters that implement their ports. The application layer carries no
 * Spring annotations, so every service bean is declared here.
 */
@Configuration(proxyBeanMethods = false)
public class UserApplicationConfig {

    @Bean
    AuthorizationQueryService authorizationQueryService(
        UserReader users,
        UserRoleReader userRoles,
        RoleReader roles,
        RoleMenuReader roleMenus,
        MenuAuthorityReader menuAuthorities,
        AuthorityReader authorities,
        RoleDataPermissionReader dataPermissions) {
        return new AuthorizationQueryService(
            users, userRoles, roles, roleMenus, menuAuthorities, authorities, dataPermissions);
    }

    @Bean
    AuthorityService authorityService(
        AuthorityReader authorities,
        MenuAuthorityReader menuAuthorities,
        AuthorityWriter authorityWriter,
        RoleReader roles) {
        return new AuthorityServiceImpl(authorities, menuAuthorities, authorityWriter, roles);
    }

    @Bean
    MenuService menuService(
        MenuReader menus, RoleMenuReader roleMenus, MenuWriter menuWriter, RoleReader roles) {
        return new MenuServiceImpl(menus, roleMenus, menuWriter, roles);
    }

    @Bean
    MenuAuthorityService menuAuthorityService(
        AuthorityWriter authorityWriter,
        MenuAuthorityReader menuAuthorities,
        AuthorityReader authorities,
        RoleReader roles) {
        return new MenuAuthorityServiceImpl(authorityWriter, menuAuthorities, authorities, roles);
    }

    @Bean
    RoleService roleService(
        RoleReader roles,
        RoleMenuReader roleMenus,
        UserRoleReader userRoles,
        RoleDataPermissionReader dataPermissions,
        RoleWriter roleWriter,
        AuthorizationQueryService authorizationQueries) {
        return new RoleServiceImpl(
            roles, roleMenus, userRoles, dataPermissions, roleWriter, authorizationQueries);
    }

    @Bean
    RoleMenuService roleMenuService(
        MenuReader menus, RoleMenuReader roleMenus, RoleMenuWriter roleMenuWriter, RoleReader roles) {
        return new RoleMenuServiceImpl(menus, roleMenus, roleMenuWriter, roles);
    }

    @Bean
    RoleDataPermissionService roleDataPermissionService(
        RoleReader roles,
        RoleDataPermissionReader dataPermissions,
        RoleDataPermissionWriter dataPermissionWriter) {
        return new RoleDataPermissionServiceImpl(roles, dataPermissions, dataPermissionWriter);
    }

    @Bean
    UserRoleService userRoleService(
        RoleReader roles, UserRoleReader userRoles, RoleDataPermissionReader dataPermissions) {
        return new UserRoleServiceImpl(roles, userRoles, dataPermissions);
    }

    @Bean
    UserService userService(
        UserReader users,
        UserWriter userWriter,
        PasswordHasher passwordHasher,
        RoleReader roles,
        UserRoleReader userRoles,
        UserRoleService userRoleService) {
        return new UserServiceImpl(
            users, userWriter, passwordHasher, roles, userRoles, userRoleService);
    }

    @Bean
    UserIdentityService userIdentityService(
        UserReader users,
        UserIdentityWriter identityWriter,
        AuthorizationQueryService authorizationQueries,
        @Value("${megalith.user.password-lock.batch-size:100}") int unlockBatchSize) {
        return new UserIdentityServiceImpl(
            users, identityWriter, authorizationQueries, unlockBatchSize);
    }

    @Bean
    RegistrationService registrationService(
        RegistrationTokenStore tokens,
        UserReader users,
        UserService userService,
        @Value("${megalith.blog.register.page-prefix}") String registerPagePrefix) {
        return new RegistrationServiceImpl(tokens, users, userService, registerPagePrefix);
    }

    @Bean
    UserAssetService userAssetService(
        RegistrationTokenStore tokens, UserAssetStorage storage) {
        return new UserAssetServiceImpl(tokens, storage);
    }

    @Bean
    UserExportService userExportService(UserReader users) {
        return new UserExportServiceImpl(users);
    }
}
