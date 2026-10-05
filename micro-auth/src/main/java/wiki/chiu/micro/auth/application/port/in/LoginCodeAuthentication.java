package wiki.chiu.micro.auth.application.port.in;

import wiki.chiu.micro.auth.application.model.CodeVerification;
import wiki.chiu.micro.auth.application.model.LoginChannel;

/**
 * Verifies a one-time login code that was delivered by e-mail or SMS.
 */
public interface LoginCodeAuthentication {

    /**
     * @param channel how the code was delivered
     * @param principal the e-mail address or phone number the code was issued for
     * @param presentedCode the code the caller submitted
     * @return the outcome of the comparison
     */
    CodeVerification authenticate(LoginChannel channel, String principal, String presentedCode);
}
