package org.chiu.micro.gateway.server;

import java.util.List;

import org.chiu.micro.gateway.req.AuthorityEntityReq;
import org.chiu.micro.gateway.req.ImgUploadReq;
import org.chiu.micro.gateway.req.MenuEntityReq;
import org.chiu.micro.gateway.req.RoleEntityReq;
import org.chiu.micro.gateway.req.UserEntityRegisterReq;
import org.chiu.micro.gateway.req.UserEntityReq;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface UserServer {

    @GetExchange("/authority/list")
    byte[] findAllAuthority();

    @GetExchange("/authority/info/{id}")
    byte[] findByAuthorityId(@PathVariable Long id);

    @PostExchange("/authority/save")
    byte[] saveOrUpdateAuthority(@RequestBody AuthorityEntityReq req);

    @PostExchange("/authority/delete")
    byte[] deleteAuthorities(@RequestBody List<Long> ids);

    @GetExchange("/authority/download")
    byte[] downloadAuthorities();

    @GetExchange("/menu/info/{id}")
    byte[] findByMenuId(@PathVariable Long id);

    @GetExchange("/menu/list")
    byte[] menuTree();

    @PostExchange("/menu/save")
    byte[] saveOrUpdateMenu(@RequestBody MenuEntityReq req);

    @PostExchange("/menu/delete/{id}")
    byte[] deleteMenu(@PathVariable Long id);

    @GetExchange("/menu/download")
    byte[] downloadMenu();

    @GetExchange("/role/info/{id}")
    byte[] infoRole(@PathVariable Long id);

    @GetExchange("/role/roles")
    byte[] getRolePage(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size);

    @PostExchange("/role/save")
    byte[] saveOrUpdateRole(@RequestBody RoleEntityReq role);

    @PostExchange("/role/delete")
    byte[] deleteRole(@RequestBody List<Long> ids);

    @PostExchange("/role/menu/{roleId}")
    byte[] saveMenu(@PathVariable Long roleId, @RequestBody List<Long> menuIds);

    @GetExchange("/role/menu/{roleId}")
    byte[] getMenusInfo(@PathVariable Long roleId);

    @PostExchange("/role/authority/{roleId}")
    byte[] saveAuthority(@PathVariable Long roleId, @RequestBody List<Long> authorityIds);

    @GetExchange("/role/authority/{roleId}")
    byte[] getAuthoritiesInfo(@PathVariable Long roleId);

    @GetExchange("/role/download")
    byte[] downloadRole();

    @GetExchange("/role/valid/all")
    byte[] getValidAll();

    @GetExchange("/user/auth/register/page")
    byte[] getRegisterPage(@RequestParam String username);

    @GetExchange("/user/register/check")
    byte[] checkRegisterPage(@RequestParam String token);

    @PostExchange("/user/register/save")
    byte[] saveRegisterPage(@RequestParam String token, @RequestBody UserEntityRegisterReq userEntityRegisterReq);

    @PostExchange("/user/register/image/upload")
    byte[] imageUpload(@RequestParam String token, @RequestBody ImgUploadReq image);

    @GetExchange("/user/register/image/delete")
    byte[] imageDelete(@RequestParam String token, @RequestParam String url);

    @PostExchange("/user/save")
    byte[] saveOrUpdateUser(@RequestBody UserEntityReq userEntityReq);

    @GetExchange("/user/page/{currentPage}")
    byte[] listPageUser(@PathVariable Integer currentPage, @RequestParam(required = false) Integer size);

    @PostExchange("/user/delete")
    byte[] deleteUsers(@RequestBody List<Long> ids);

    @GetExchange("/user/info/{id}")
    byte[] findByIdUser(@PathVariable Long id);

    @GetExchange("/user/download")
    byte[] downloadUser();


}
