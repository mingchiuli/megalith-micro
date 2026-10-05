package wiki.chiu.micro.user.application.port.in;

import wiki.chiu.micro.user.application.model.RegistrationDraft;

public interface RegistrationService {

    String issuePage(String username);

    boolean isPageValid(String token);

    void register(RegistrationDraft request);
}
