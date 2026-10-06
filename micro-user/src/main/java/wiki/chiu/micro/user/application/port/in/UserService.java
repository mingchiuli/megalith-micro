package wiki.chiu.micro.user.application.port.in;

import java.util.List;

import wiki.chiu.micro.user.application.model.Page;
import wiki.chiu.micro.user.application.model.UserDraft;
import wiki.chiu.micro.user.application.model.UserView;

public interface UserService {

    void saveOrUpdate(UserDraft userEntityReq);

    Page<UserView> listPage(Integer currentPage, Integer size);

    UserView findInfo(Long id);

    void deleteUsers(List<Long> ids);
}
