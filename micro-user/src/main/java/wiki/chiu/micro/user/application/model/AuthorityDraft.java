package wiki.chiu.micro.user.application.model;

import wiki.chiu.micro.user.domain.Authority;

/**
 * An authority as submitted by a client, before it is merged with the stored state.
 */
public record AuthorityDraft(
    Long id,
    String code,
    String remark,
    String prototype,
    String methodType,
    String routePattern,
    String serviceHost,
    Integer servicePort,
    Integer type,
    Integer status) {

    public Authority mergeInto(Authority dealAuthority) {
        return new Authority(
            id,
            code,
            remark,
            prototype,
            methodType,
            routePattern,
            serviceHost,
            servicePort,
            dealAuthority.created(),
            dealAuthority.updated(),
            type,
            status);
    }
}
