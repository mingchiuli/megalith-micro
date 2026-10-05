package wiki.chiu.micro.auth.application.port.in;

import wiki.chiu.micro.auth.application.model.LoginAccount;

/**
 * Loads the account and its effective roles for a submitted login name.
 */
public interface LoginAccountLookup {

    /**
     * @param loginName the submitted username, e-mail address, or phone number
     * @return the matching account with only the roles that exist and are enabled
     */
    LoginAccount byLoginName(String loginName);
}
