package wiki.chiu.micro.exhibit.application.port.out;

import java.util.List;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;

public interface BlogCatalog {

  List<BlogEntityRpcVo> findAllById(List<Long> ids);
}
