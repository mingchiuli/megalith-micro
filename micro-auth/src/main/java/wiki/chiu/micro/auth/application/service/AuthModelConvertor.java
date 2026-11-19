package wiki.chiu.micro.auth.application.service;

import wiki.chiu.micro.auth.application.model.UserAccount;
import wiki.chiu.micro.auth.application.model.UserInfo;

public final class AuthModelConvertor {

    private AuthModelConvertor() {
    }

    public static UserInfo toUserInfo(UserAccount user) {
        return new UserInfo(user.id(), user.nickname(), user.avatar());
    }
}
