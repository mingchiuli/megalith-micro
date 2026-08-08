package wiki.chiu.micro.auth.dto;

public record LoginRequest(LoginType loginType, String principal, String credential) {}
