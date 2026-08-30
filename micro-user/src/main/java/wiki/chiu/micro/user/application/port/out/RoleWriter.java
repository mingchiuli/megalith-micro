package wiki.chiu.micro.user.application.port.out;

import java.util.List;
import wiki.chiu.micro.user.domain.RoleEntity;

public interface RoleWriter {

  void saveOrUpdate(RoleEntity role, List<String> affectedCodes);

  void delete(List<Long> ids, List<String> roleCodes);
}
