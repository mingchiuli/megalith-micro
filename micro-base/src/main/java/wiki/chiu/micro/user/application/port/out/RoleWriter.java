package wiki.chiu.micro.user.application.port.out;

import java.util.List;

import wiki.chiu.micro.user.domain.Role;

public interface RoleWriter {

    void saveOrUpdate(Role role, String previousCode, List<String> affectedCodes);

    void delete(List<Long> ids, List<String> roleCodes);
}
