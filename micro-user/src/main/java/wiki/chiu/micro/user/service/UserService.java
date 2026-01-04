package wiki.chiu.micro.user.service;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.req.UserEntityRegisterReq;
import wiki.chiu.micro.user.req.UserEntityReq;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import wiki.chiu.micro.user.vo.UserEntityVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
public interface UserService {

    void updateLoginTime(String username, LocalDateTime time);

    void changeUserStatusByUsername(String username, Integer status);

    String getRegisterPage(String username);

    String imageUpload(String token, MultipartFile req);

    void imageDelete(String token, String url);

    Boolean checkRegisterPage(String token);

    void download(HttpServletResponse response);

    UserEntityRpcVo findById(Long userId);

    UserEntityRpcVo findByEmail(String email);

    UserEntityRpcVo findByPhone(String phone);

    UserEntityRpcVo findByUsernameOrEmailOrPhone(String username);

    void saveRegisterPage(UserEntityRegisterReq req);

    void saveOrUpdate(UserEntityReq userEntityReq);

    PageAdapter<UserEntityVo> listPage(Integer currentPage, Integer size);

    void deleteUsers(List<Long> ids);

    UserEntityVo findInfo(Long id);
}
