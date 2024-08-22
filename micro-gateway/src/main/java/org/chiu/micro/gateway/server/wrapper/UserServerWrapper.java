package org.chiu.micro.gateway.server.wrapper;

import java.util.List;

import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.page.PageAdapter;
import org.chiu.micro.gateway.req.AuthorityEntityReq;
import org.chiu.micro.gateway.req.ImgUploadReq;
import org.chiu.micro.gateway.req.MenuEntityReq;
import org.chiu.micro.gateway.req.RoleEntityReq;
import org.chiu.micro.gateway.req.UserEntityRegisterReq;
import org.chiu.micro.gateway.req.UserEntityReq;
import org.chiu.micro.gateway.server.UserServer;
import org.chiu.micro.gateway.vo.AuthorityVo;
import org.chiu.micro.gateway.vo.MenuDisplayVo;
import org.chiu.micro.gateway.vo.MenuEntityVo;
import org.chiu.micro.gateway.vo.RoleAuthorityVo;
import org.chiu.micro.gateway.vo.RoleEntityVo;
import org.chiu.micro.gateway.vo.RoleMenuVo;
import org.chiu.micro.gateway.vo.UserEntityVo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletOutputStream;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * UserServerWrapper
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/sys")
public class UserServerWrapper {

    private final UserServer userServer;

    @GetMapping("/authority/list")
    public Result<List<AuthorityVo>> findAllAuthority() {
        return userServer.findAllAuthority();
    }

    @GetMapping("/authority/info/{id}")
    public Result<AuthorityVo> findByAuthorityId(@PathVariable Long id) {
        return userServer.findByAuthorityId(id);
    }

    @PostMapping("/authority/save")
    public Result<Void> saveOrUpdateAuthority(@RequestBody AuthorityEntityReq req) {
        return userServer.saveOrUpdateAuthority(req);
    }

    @PostMapping("/authority/delete")
    public Result<Void> deleteAuthorities(@RequestBody List<Long> ids) {
        return userServer.deleteAuthorities(ids);
    }

    @SneakyThrows
    @GetMapping("/authority/download")
    public void downloadAuthorities(HttpServletResponse response) {
        byte[] data = userServer.downloadAuthorities();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLength(data.length);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

    @GetMapping("/menu/info/{id}")
    public Result<MenuEntityVo> findByMenuId(@PathVariable Long id) {
        return userServer.findByMenuId(id);
    }

    @GetMapping("/menu/list")
    public Result<List<MenuDisplayVo>> listMenu() {
        return userServer.menuTree();
    }

    @PostMapping("/menu/save")
    public Result<Void> saveOrUpdateMenu(@RequestBody MenuEntityReq menu) {
        return userServer.saveOrUpdateMenu(menu);
    }

    @PostMapping("/menu/delete/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        return userServer.deleteMenu(id);
    }

    @GetMapping("/menu/download")
    @SneakyThrows
    public void downloadMenu(HttpServletResponse response) {
        byte[] data = userServer.downloadMenu();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLength(data.length);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

    @GetMapping("/role/info/{id}")
    public Result<RoleEntityVo> info(@PathVariable Long id) {
        return userServer.infoRole(id);
    }

    @GetMapping("/role/roles")
    public Result<PageAdapter<RoleEntityVo>> getPage(@RequestParam(required = false) Integer currentPage,
                                                     @RequestParam(required = false) Integer size) {
        return userServer.getRolePage(currentPage, size);
    }

    @PostMapping("/role/save")
    public Result<Void> saveOrUpdateRole(@RequestBody RoleEntityReq role) {
        return userServer.saveOrUpdateRole(role);
    }

    @PostMapping("/role/delete")
    public Result<Void> deleteRole(@RequestBody List<Long> ids) {
        return userServer.deleteRole(ids);
    }

    @PostMapping("/role/menu/{roleId}")
    public Result<Void> saveMenu(@PathVariable Long roleId,
                                 @RequestBody List<Long> menuIds) {
        return userServer.saveMenu(roleId, menuIds);
    }

    @GetMapping("/role/menu/{roleId}")
    public Result<List<RoleMenuVo>> getMenusInfo(@PathVariable Long roleId) {
        return userServer.getMenusInfo(roleId);
    }

    @PostMapping("/role/authority/{roleId}")
    public Result<Void> saveAuthority(@PathVariable Long roleId,
                                      @RequestBody List<Long> authorityIds) {
        return userServer.saveAuthority(roleId, authorityIds);
    }

    @GetMapping("/role/authority/{roleId}")
    public Result<List<RoleAuthorityVo>> getAuthoritiesInfo(@PathVariable Long roleId) {
        return userServer.getAuthoritiesInfo(roleId);
    }

    @GetMapping("/role/download")
    @SneakyThrows
    public void downloadRole(HttpServletResponse response) {
        byte[] data = userServer.downloadRole();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentLength(data.length);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

    @GetMapping("/role/valid/all")
    public Result<List<RoleEntityVo>> getValidAll() {
        return userServer.getValidAll();
    }

    @GetMapping("/user/auth/register/page")
    public Result<String> getRegisterPage(@RequestParam String username) {
        return userServer.getRegisterPage(username);
    }

    @GetMapping("/user/register/check")
    public Result<Boolean> checkRegisterPage(@RequestParam String token) {
        return userServer.checkRegisterPage(token);
    }

    @PostMapping("/user/register/save")
    public Result<Void> saveRegisterPage(@RequestParam String token,
                                         @RequestBody UserEntityRegisterReq userEntityRegisterReq) {
        return userServer.saveRegisterPage(token, userEntityRegisterReq);
    }

    @PostMapping("/user/register/image/upload")
    @SneakyThrows
    public Result<String> imageUpload(@RequestParam MultipartFile image,
                                      @RequestParam String token) {
        var req = new ImgUploadReq();
        req.setData(image.getBytes());
        req.setFileName(image.getOriginalFilename());
        return userServer.imageUpload(token, req);
    }

    @GetMapping("/user/register/image/delete")
    public Result<Void> imageDelete(@RequestParam String url,
                                    @RequestParam String token) {
        return userServer.imageDelete(token, url);
    }

    @PostMapping("/user/save")
    public Result<Void> saveOrUpdateUser(@RequestBody UserEntityReq userEntityReq) {
        return userServer.saveOrUpdateUser(userEntityReq);
    }

    @GetMapping("/user/page/{currentPage}")
    public Result<PageAdapter<UserEntityVo>> listPageUser(@PathVariable Integer currentPage,
                                                          @RequestParam(required = false) Integer size) {
        return userServer.listPageUser(currentPage, size);
    }

    @PostMapping("/user/delete")
    public Result<Void> deleteUsers(@RequestBody List<Long> ids) {
        return userServer.deleteUsers(ids);
    }

    @GetMapping("/user/info/{id}")
    public Result<UserEntityVo> findByIdUser(@PathVariable Long id) {
        return userServer.findByIdUser(id);
    }

    @GetMapping("/user/download")
    @SneakyThrows
    public void downloadUser(HttpServletResponse response) {
        byte[] data = userServer.downloadUser();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLength(data.length);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }
}