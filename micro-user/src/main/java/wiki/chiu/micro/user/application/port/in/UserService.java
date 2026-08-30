package wiki.chiu.micro.user.application.port.in;

import java.util.List;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.user.req.UserEntityReq;
import wiki.chiu.micro.user.vo.UserEntityVo;

/**
 * @author mingchiuli
 * @create 2022-12-04 4:55 pm
 */
public interface UserService {

  void saveOrUpdate(UserEntityReq userEntityReq);

  PageAdapter<UserEntityVo> listPage(Integer currentPage, Integer size);

  void deleteUsers(List<Long> ids);

  UserEntityVo findInfo(Long id);
}
