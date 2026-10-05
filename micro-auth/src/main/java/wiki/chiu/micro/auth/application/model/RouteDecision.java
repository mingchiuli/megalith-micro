package wiki.chiu.micro.auth.application.model;

import wiki.chiu.micro.common.security.AuthPrincipal;

/**
 * Where the gateway must send an authorized route and who the caller is.
 */
public record RouteDecision(String serviceHost, Integer servicePort, AuthPrincipal principal) {
}
