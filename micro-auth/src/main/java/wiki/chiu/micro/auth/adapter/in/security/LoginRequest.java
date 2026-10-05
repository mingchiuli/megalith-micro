package wiki.chiu.micro.auth.adapter.in.security;

public record LoginRequest(LoginType loginType, String principal, String credential) {
}
