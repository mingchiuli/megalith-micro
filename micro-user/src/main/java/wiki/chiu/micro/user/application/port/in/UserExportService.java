package wiki.chiu.micro.user.application.port.in;

import java.io.OutputStream;

public interface UserExportService {

    void write(OutputStream outputStream);
}
