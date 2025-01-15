package wiki.chiu.micro.user.service;

import java.util.List;

/**
 * @Author limingjiu
 * @Date 2024/5/29 22:12
 **/
public interface UserRoleService {

    List<String> findRoleCodesByUserId(Long userId);
}
