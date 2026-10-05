package wiki.chiu.micro.auth.application.port.out;

/**
 * Resolves the caller of a routed request from the token the gateway forwards.
 */
public interface RouteTokenReader {

    /**
     * @param routeMapping the route being authorized, which decides which token type is accepted
     * @param token the bearer token, or empty when the caller sent none
     * @return the caller's user id, or null when no token was supplied
     * @throws wiki.chiu.micro.common.exception.MissException when the token is malformed, expired,
     *     of the wrong type, or bound to a different room
     */
    Long resolveUserId(String routeMapping, String token);
}
