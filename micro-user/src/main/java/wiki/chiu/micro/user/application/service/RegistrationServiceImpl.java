package wiki.chiu.micro.user.application.service;
import static wiki.chiu.micro.common.constant.Const.USER;
import static wiki.chiu.micro.common.enums.StatusEnum.NORMAL;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import wiki.chiu.micro.common.enums.StatusEnum;
import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.user.application.model.RegistrationDraft;
import wiki.chiu.micro.user.application.model.UserDraft;
import wiki.chiu.micro.user.application.port.in.RegistrationService;
import wiki.chiu.micro.user.application.port.in.UserService;
import wiki.chiu.micro.user.application.port.out.RegistrationTokenStore;
import wiki.chiu.micro.user.application.port.out.UserReader;
import wiki.chiu.micro.user.domain.PhonePlaceholderGenerator;

public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationTokenStore tokens;

    private final UserReader users;

    private final UserService userService;

    private final String registerPagePrefix;

    public RegistrationServiceImpl(
        RegistrationTokenStore tokens,
        UserReader users,
        UserService userService,
        String registerPagePrefix) {
        this.tokens = tokens;
        this.users = users;
        this.userService = userService;
        this.registerPagePrefix = registerPagePrefix;
    }

    @Override
    public String issuePage(String username) {
        String token = tokens.issue(username);
        return hasLength(username)
            ? registerPagePrefix
                + token
                + "?username="
                + URLEncoder.encode(username, StandardCharsets.UTF_8)
            : registerPagePrefix + token;
    }

    @Override
    public boolean isPageValid(String token) {
        return tokens.exists(token);
    }

    @Override
    public void register(RegistrationDraft request) {
        validatePolicy(request);
        RegistrationDraft normalized =
            hasLength(request.phone())
                ? request
                : request.withPhone(PhonePlaceholderGenerator.generate());
        UserDraft user = toUserDraft(normalized);
        tokens.consumeForUsername(request.token(), request.username());
        userService.saveOrUpdate(user);
    }

    private void validatePolicy(RegistrationDraft request) {
        users
            .findByUsername(request.username())
            .filter(user -> StatusEnum.HIDE.getCode().equals(user.status()))
            .ifPresent(
                _ -> {
                    throw new ValidationException("registration arguments are invalid");
                });
    }

    private UserDraft toUserDraft(RegistrationDraft request) {
        List<String> roles = List.of(USER);
        return users
            .findByUsername(request.username())
            .map(user -> request.toUserDraft(user.id(), NORMAL.getCode(), roles))
            .orElseGet(() -> request.toUserDraft(null, NORMAL.getCode(), roles));
    }

    private static boolean hasLength(String value) {
        return value != null && !value.isEmpty();
    }
}
