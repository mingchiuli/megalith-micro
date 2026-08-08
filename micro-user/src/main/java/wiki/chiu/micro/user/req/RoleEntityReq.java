package wiki.chiu.micro.user.req;

import java.util.Optional;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:57 pm
 */
public record RoleEntityReq(
    Optional<Long> id, String name, String code, String remark, Integer status) {}
