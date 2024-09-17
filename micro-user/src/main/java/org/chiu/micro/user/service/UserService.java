package org.chiu.micro.user.service;

import org.chiu.micro.user.vo.UserEntityRpcVo;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
public interface UserService {

    void updateLoginTime(String username, LocalDateTime time);

    void changeUserStatusByUsername(String username, Integer status);

    String getRegisterPage(String username);

    SseEmitter imageUpload(String token, MultipartFile req);

    void imageDelete(String token, String url);

    Boolean checkRegisterPage(String token);

    void download(HttpServletResponse response);

    UserEntityRpcVo findById(Long userId);

    UserEntityRpcVo findByEmail(String email);

    UserEntityRpcVo findByPhone(String phone);

    UserEntityRpcVo findByUsernameOrEmailOrPhone(String username);
    
    void unlockUser();
}
