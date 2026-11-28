package wiki.chiu.micro.user.application.service;

import wiki.chiu.micro.user.application.model.UserExport;
import wiki.chiu.micro.user.application.port.in.UserExportService;
import wiki.chiu.micro.user.application.port.out.UserReader;

public class UserExportServiceImpl implements UserExportService {

    private final UserReader users;

    public UserExportServiceImpl(UserReader users) {
        this.users = users;
    }

    @Override
    public UserExport export() {
        return new UserExport(users.findAll());
    }
}
