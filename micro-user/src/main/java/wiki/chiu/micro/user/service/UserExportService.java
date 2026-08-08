package wiki.chiu.micro.user.service;

import java.io.OutputStream;

public interface UserExportService {

  void write(OutputStream outputStream);
}
