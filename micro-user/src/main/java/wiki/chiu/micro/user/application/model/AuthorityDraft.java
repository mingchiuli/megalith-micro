package wiki.chiu.micro.user.application.model;

/**
 * An authority as submitted by a client, before it is merged with the stored state.
 */
public record AuthorityDraft(
    Long id,
    String code,
    String remark,
    String protocol,
    String methodType,
    String routePattern,
    String serviceHost,
    Integer servicePort,
    Integer type,
    Integer status) {

}
