package wiki.chiu.micro.user.service;

import java.time.LocalDateTime;
import java.util.List;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.vo.UserEntityRpcVo;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.vo.UserEntityVo;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
public interface UserService {

  void updateLoginTime(String username, LocalDateTime time);

  void changeUserStatusByUsername(String username, Integer status);

  UserEntityRpcVo findById(Long userId);

  UserEntityRpcVo findByEmail(String email);

  UserEntityRpcVo findByPhone(String phone);

  UserEntityRpcVo findByUsernameOrEmailOrPhone(String username);

  void saveOrUpdate(UserEntityReq userEntityReq);

  PageAdapter<UserEntityVo> listPage(Integer currentPage, Integer size);

  void deleteUsers(List<Long> ids);

  UserEntityVo findInfo(Long id);
}
