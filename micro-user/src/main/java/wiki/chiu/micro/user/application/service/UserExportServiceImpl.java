package wiki.chiu.micro.user.application.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.common.export.SQLUtils;
import wiki.chiu.micro.user.application.model.SqlTables;
import wiki.chiu.micro.user.application.port.in.UserExportService;
import wiki.chiu.micro.user.application.port.out.UserReader;

@Service
public class UserExportServiceImpl implements UserExportService {

    private final UserReader users;

    public UserExportServiceImpl(UserReader users) {
        this.users = users;
    }

    @Override
    public void write(OutputStream outputStream) {
        String sql = SQLUtils.insertSql(users.findAll(), SqlTables.USER);
        try {
            outputStream.write(sql.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to write user export", exception);
        }
    }
}
