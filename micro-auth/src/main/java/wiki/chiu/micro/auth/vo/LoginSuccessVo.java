package wiki.chiu.micro.auth.vo;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2023-04-19 1:47 am
 */
public record LoginSuccessVo(

        String accessToken,

        String refreshToken) implements Serializable {

    public static LoginSuccessVoBuilder builder() {
        return new LoginSuccessVoBuilder(null, null);
    }

    public record LoginSuccessVoBuilder(
            String accessToken,
            String refreshToken
    ) {

        public LoginSuccessVoBuilder accessToken(String accessToken) {
            return new LoginSuccessVoBuilder(accessToken, refreshToken);
        }

        public LoginSuccessVoBuilder refreshToken(String refreshToken) {
            return new LoginSuccessVoBuilder(accessToken, refreshToken);
        }

        public LoginSuccessVo build() {
            return new LoginSuccessVo(accessToken, refreshToken);
        }

    }
}
