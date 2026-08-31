package wiki.chiu.micro.user.application.port.in;

import java.util.List;

import wiki.chiu.micro.common.lang.DataPermissionEnum;

/**
 * @Author limingjiu @Date 2024/5/29 22:12
 */
public interface UserRoleService {

    List<String> findRoleCodesByUserId(Long userId);

    List<DataPermissionEnum> findDataPermissionsByUserId(Long userId);
}
