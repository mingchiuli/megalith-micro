package wiki.chiu.micro.auth.application.port.out;

import java.util.List;

import wiki.chiu.micro.auth.application.model.UserAccess;
import wiki.chiu.micro.auth.application.model.UserAccount;

public interface UserDirectory {

    UserAccount findById(Long userId);

    /**
     * @param loginName the submitted username, e-mail address, or phone number
     * @return the matching account, credentials included
     */
    UserAccount findByLoginName(String loginName);

    void findByEmail(String email);

    void findByPhone(String phone);

    UserAccess findUserAccess(Long userId);

    /**
     * A role only counts when it exists and is enabled.
     *
     * @return the codes of the user's roles that exist and are enabled
     */
    List<String> findEnabledRoleCodesOf(Long userId);

    /**
     * @param roleCodes the caller's granted role codes
     * @return the subset that exists and is enabled
     */
    List<String> findEnabledRoleCodes(List<String> roleCodes);

    void lockAfterPasswordFailures(Long userId);

    void updateLoginTime(String username);
}
