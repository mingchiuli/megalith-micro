package org.chiu.micro.auth.vo;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2023-04-19 1:47 am
 */
public class LoginSuccessVo implements Serializable {
    private String accessToken;

    private String refreshToken;

    LoginSuccessVo(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static LoginSuccessVoBuilder builder() {
        return new LoginSuccessVoBuilder();
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof LoginSuccessVo)) return false;
        final LoginSuccessVo other = (LoginSuccessVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$accessToken = this.getAccessToken();
        final Object other$accessToken = other.getAccessToken();
        if (this$accessToken == null ? other$accessToken != null : !this$accessToken.equals(other$accessToken))
            return false;
        final Object this$refreshToken = this.getRefreshToken();
        final Object other$refreshToken = other.getRefreshToken();
        if (this$refreshToken == null ? other$refreshToken != null : !this$refreshToken.equals(other$refreshToken))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof LoginSuccessVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $accessToken = this.getAccessToken();
        result = result * PRIME + ($accessToken == null ? 43 : $accessToken.hashCode());
        final Object $refreshToken = this.getRefreshToken();
        result = result * PRIME + ($refreshToken == null ? 43 : $refreshToken.hashCode());
        return result;
    }

    public String toString() {
        return "LoginSuccessVo(accessToken=" + this.getAccessToken() + ", refreshToken=" + this.getRefreshToken() + ")";
    }

    public static class LoginSuccessVoBuilder {
        private String accessToken;
        private String refreshToken;

        LoginSuccessVoBuilder() {
        }

        public LoginSuccessVoBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public LoginSuccessVoBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public LoginSuccessVo build() {
            return new LoginSuccessVo(this.accessToken, this.refreshToken);
        }

        public String toString() {
            return "LoginSuccessVo.LoginSuccessVoBuilder(accessToken=" + this.accessToken + ", refreshToken=" + this.refreshToken + ")";
        }
    }
}
