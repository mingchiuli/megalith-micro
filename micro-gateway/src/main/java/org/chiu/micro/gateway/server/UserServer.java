package org.chiu.micro.gateway.server;

import java.util.List;

import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.page.PageAdapter;
import org.chiu.micro.gateway.req.AuthorityEntityReq;
import org.chiu.micro.gateway.req.ImgUploadReq;
import org.chiu.micro.gateway.req.MenuEntityReq;
import org.chiu.micro.gateway.req.RoleEntityReq;
import org.chiu.micro.gateway.req.UserEntityRegisterReq;
import org.chiu.micro.gateway.req.UserEntityReq;
import org.chiu.micro.gateway.vo.AuthorityVo;
import org.chiu.micro.gateway.vo.MenuDisplayVo;
import org.chiu.micro.gateway.vo.MenuEntityVo;
import org.chiu.micro.gateway.vo.RoleAuthorityVo;
import org.chiu.micro.gateway.vo.RoleEntityVo;
import org.chiu.micro.gateway.vo.RoleMenuVo;
import org.chiu.micro.gateway.vo.UserEntityVo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface UserServer {

    @GetExchange("/authority/list")
    Result<List<AuthorityVo>> findAllAuthority();

    @GetExchange("/authority/info/{id}")
    Result<AuthorityVo> findByAuthorityId(@PathVariable Long id);

    @PostExchange("/authority/save")
    Result<Void> saveOrUpdateAuthority(@RequestBody AuthorityEntityReq req);

    @PostExchange("/authority/delete")
    Result<Void> deleteAuthorities(@RequestBody List<Long> ids);

    @GetExchange("/authority/download")
    byte[] downloadAuthorities();

    @GetExchange("/menu/info/{id}")
    Result<MenuEntityVo> findByMenuId(@PathVariable Long id);

    @GetExchange("/menu/list")
    Result<List<MenuDisplayVo>> menuTree();

    @PostExchange("/menu/save")
    Result<Void> saveOrUpdateMenu(@RequestBody MenuEntityReq req);

    @PostExchange("/menu/delete/{id}")
    Result<Void> deleteMenu(@PathVariable Long id);

    @GetExchange("/menu/download")
    byte[] downloadMenu();

    @GetExchange("/role/info/{id}")
    Result<RoleEntityVo> infoRole(@PathVariable Long id);

    @GetExchange("/role/roles")
    Result<PageAdapter<RoleEntityVo>> getRolePage(@RequestParam(required = false) Integer currentPage, @RequestParam(required = false) Integer size);

    @PostExchange("/role/save")
    Result<Void> saveOrUpdateRole(@RequestBody RoleEntityReq role);

    @PostExchange("/role/delete")
    Result<Void> deleteRole(@RequestBody List<Long> ids);

    @PostExchange("/role/menu/{roleId}")
    Result<Void> saveMenu(@PathVariable Long roleId, @RequestBody List<Long> menuIds);

    @GetExchange("/role/menu/{roleId}")
    Result<List<RoleMenuVo>> getMenusInfo(@PathVariable Long roleId);

    @PostExchange("/role/authority/{roleId}")
    Result<Void> saveAuthority(@PathVariable Long roleId, @RequestBody List<Long> authorityIds);

    @GetExchange("/role/authority/{roleId}")
    Result<List<RoleAuthorityVo>> getAuthoritiesInfo(@PathVariable Long roleId);

    @GetExchange("/role/download")
    byte[] downloadRole();

    @GetExchange("/role/valid/all")
    Result<List<RoleEntityVo>> getValidAll();

    @GetExchange("/user/auth/register/page")
    Result<String> getRegisterPage(@RequestParam String username);

    @GetExchange("/user/register/check")
    Result<Boolean> checkRegisterPage(@RequestParam String token);

    @PostExchange("/user/register/save")
    Result<Void> saveRegisterPage(@RequestParam String token, @RequestBody UserEntityRegisterReq userEntityRegisterReq);

    @PostExchange("/user/register/image/upload")
    Result<String> imageUpload(@RequestParam String token, @RequestBody ImgUploadReq image);

    @GetExchange("/user/register/image/delete")
    Result<Void> imageDelete(@RequestParam String token, @RequestParam String url);

    @PostExchange("/user/save")
    Result<Void> saveOrUpdateUser(@RequestBody UserEntityReq userEntityReq);

    @GetExchange("/user/page/{currentPage}")
    Result<PageAdapter<UserEntityVo>> listPageUser(@PathVariable Integer currentPage, @RequestParam(required = false) Integer size);

    @PostExchange("/user/delete")
    Result<Void> deleteUsers(@RequestBody List<Long> ids);

    @GetExchange("/user/info/{id}")
    Result<UserEntityVo> findByIdUser(@PathVariable Long id);

    @GetExchange("/user/download")
    byte[] downloadUser();


}
