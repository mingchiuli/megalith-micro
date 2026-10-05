package wiki.chiu.micro.auth.application.port.out;

/**
 * Mints the signed tokens this service hands out.
 */
public interface TokenEncoder {

    String accessToken(Long userId);

    String refreshToken(Long userId);

    String webSocketTicket(Long userId, String roomId);
}
