package wiki.chiu.micro.blog.service;

import java.io.OutputStream;
import java.util.List;
import wiki.chiu.micro.blog.req.BlogDownloadReq;

public interface BlogExportService {

  void write(BlogDownloadReq request, Long userId, List<String> roles, OutputStream outputStream);
}
