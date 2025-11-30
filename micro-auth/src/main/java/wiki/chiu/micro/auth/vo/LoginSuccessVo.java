package wiki.chiu.micro.auth.vo;


/**
 * @author mingchiuli
 * @create 2023-04-19 1:47 am
 */
public record LoginSuccessVo(

        String accessToken,

        String refreshToken) {

    public static LoginSuccessVoBuilder builder() {
        return new LoginSuccessVoBuilder();
    }

    public static class LoginSuccessVoBuilder {
        private String accessToken;
        private String refreshToken;


        public LoginSuccessVoBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public LoginSuccessVoBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public LoginSuccessVo build() {
            return new LoginSuccessVo(accessToken, refreshToken);
        }

    }
}
