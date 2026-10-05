package wiki.chiu.micro.auth.application.model;

/**
 * A routable authority: the pattern it protects and the service it forwards to.
 */
public record Authority(
    Long id,
    String code,
    String methodType,
    String routePattern,
    String serviceHost,
    Integer servicePort,
    Integer type) {
}
