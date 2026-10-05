package wiki.chiu.micro.user.application.model;

import wiki.chiu.micro.user.domain.Role;

/**
 * A role as submitted by a client, before it is merged with the stored state.
 */
public record RoleDraft(Long id, String name, String code, String remark, Integer status) {

    public Role mergeInto(Role dealRole) {
        return new Role(
            id, name, code, remark, dealRole.created(), dealRole.updated(), status);
    }
}
