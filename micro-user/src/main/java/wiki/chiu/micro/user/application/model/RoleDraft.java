package wiki.chiu.micro.user.application.model;

/**
 * A role as submitted by a client, before it is merged with the stored state.
 */
public record RoleDraft(Long id, String name, String code, String remark, Integer status) {

}
