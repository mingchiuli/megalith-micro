package org.chiu.micro.user.service;

import org.chiu.micro.user.req.ImgUploadReq;
import org.chiu.micro.user.vo.UserEntityRpcVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
public interface UserService {

    void updateLoginTime(String username, LocalDateTime time);

    void changeUserStatusByUsername(String username, Integer status);

    List<Long> findIdsByStatus(Integer status);

    String getRegisterPage(String username);

    String imageUpload(String token, ImgUploadReq req);

    void imageDelete(String token, String url);

    Boolean checkRegisterPage(String token);

    byte[] download();

    UserEntityRpcVo findById(Long userId);

    UserEntityRpcVo findByEmail(String email);

    UserEntityRpcVo findByPhone(String phone);

    UserEntityRpcVo findByUsernameOrEmailOrPhone(String username);
}
