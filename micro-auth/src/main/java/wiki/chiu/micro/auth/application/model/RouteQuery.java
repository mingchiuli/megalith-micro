package wiki.chiu.micro.auth.application.model;

/**
 * The route the gateway asks about.
 */
public record RouteQuery(String method, String routeMapping, String ipAddr) {
}
