package org.chiu.micro.gateway.server.wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chiu.micro.gateway.server.UserServer;
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
    public byte[] findAllAuthority() {
        return userServer.findAllAuthority();
    }

    @GetMapping("/authority/info/{id}")
    public byte[] findByAuthorityId(@PathVariable Long id) {
        return userServer.findByAuthorityId(id);
    }

    @PostMapping("/authority/save")
    public byte[] saveOrUpdateAuthority(@RequestBody byte[] data) {
        return userServer.saveOrUpdateAuthority(data);
    }

    @PostMapping("/authority/delete")
    public byte[] deleteAuthorities(@RequestBody List<Long> ids) {
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
    public byte[] findByMenuId(@PathVariable Long id) {
        return userServer.findByMenuId(id);
    }

    @GetMapping("/menu/list")
    public byte[] listMenu() {
        return userServer.menuTree();
    }

    @PostMapping("/menu/save")
    public byte[] saveOrUpdateMenu(@RequestBody byte[] data) {
        return userServer.saveOrUpdateMenu(data);
    }

    @PostMapping("/menu/delete/{id}")
    public byte[] deleteMenu(@PathVariable Long id) {
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
    public byte[] info(@PathVariable Long id) {
        return userServer.infoRole(id);
    }

    @GetMapping("/role/roles")
    public byte[] getPage(@RequestParam(required = false) Integer currentPage,
                          @RequestParam(required = false) Integer size) {
        return userServer.getRolePage(currentPage, size);
    }

    @PostMapping("/role/save")
    public byte[] saveOrUpdateRole(@RequestBody byte[] data) {
        return userServer.saveOrUpdateRole(data);
    }

    @PostMapping("/role/delete")
    public byte[] deleteRole(@RequestBody List<Long> ids) {
        return userServer.deleteRole(ids);
    }

    @PostMapping("/role/menu/{roleId}")
    public byte[] saveMenu(@PathVariable Long roleId,
                           @RequestBody List<Long> menuIds) {
        return userServer.saveMenu(roleId, menuIds);
    }

    @GetMapping("/role/menu/{roleId}")
    public byte[] getMenusInfo(@PathVariable Long roleId) {
        return userServer.getMenusInfo(roleId);
    }

    @PostMapping("/role/authority/{roleId}")
    public byte[] saveAuthority(@PathVariable Long roleId,
                                @RequestBody List<Long> authorityIds) {
        return userServer.saveAuthority(roleId, authorityIds);
    }

    @GetMapping("/role/authority/{roleId}")
    public byte[] getAuthoritiesInfo(@PathVariable Long roleId) {
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
    public byte[] getValidAll() {
        return userServer.getValidAll();
    }

    @GetMapping("/user/auth/register/page")
    public byte[] getRegisterPage(@RequestParam String username) {
        return userServer.getRegisterPage(username);
    }

    @GetMapping("/user/register/check")
    public byte[] checkRegisterPage(@RequestParam String token) {
        return userServer.checkRegisterPage(token);
    }

    @PostMapping("/user/register/save")
    public byte[] saveRegisterPage(@RequestParam String token,
                                   @RequestBody byte[] data) {
        return userServer.saveRegisterPage(token, data);
    }

    @PostMapping("/user/register/image/upload")
    @SneakyThrows
    public byte[] imageUpload(@RequestParam MultipartFile image,
                              @RequestParam String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("fileName", image.getOriginalFilename());
        params.put("data", image.getBytes());
        return userServer.imageUpload(token, params);
    }

    @GetMapping("/user/register/image/delete")
    public byte[] imageDelete(@RequestParam String url,
                              @RequestParam String token) {
        return userServer.imageDelete(token, url);
    }

    @PostMapping("/user/save")
    public byte[] saveOrUpdateUser(@RequestBody byte[] data) {
        return userServer.saveOrUpdateUser(data);
    }

    @GetMapping("/user/page/{currentPage}")
    public byte[] listPageUser(@PathVariable Integer currentPage,
                               @RequestParam(required = false) Integer size) {
        return userServer.listPageUser(currentPage, size);
    }

    @PostMapping("/user/delete")
    public byte[] deleteUsers(@RequestBody List<Long> ids) {
        return userServer.deleteUsers(ids);
    }

    @GetMapping("/user/info/{id}")
    public byte[] findByIdUser(@PathVariable Long id) {
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