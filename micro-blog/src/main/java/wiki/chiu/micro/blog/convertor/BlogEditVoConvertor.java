package wiki.chiu.micro.blog.convertor;

import java.util.List;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogPermissionsVo;

public class BlogEditVoConvertor {

  private BlogEditVoConvertor() {}

  public static BlogEditVo convert(
      BlogEntity blog,
      List<BlogEditVo.SensitiveContentVo> sensitiveContentList,
      BlogPermissionsVo permissions) {
    return BlogEditVo.builder()
        .id(blog.getId())
        .userId(blog.getUserId())
        .title(blog.getTitle())
        .description(blog.getDescription())
        .content(blog.getContent())
        .link(blog.getLink())
        .status(blog.getStatus())
        .permissions(permissions)
        .sensitiveContentList(
            sensitiveContentList.stream()
                .map(
                    item ->
                        BlogEditVo.SensitiveContentVo.builder()
                            .type(item.type())
                            .startIndex(item.startIndex())
                            .endIndex(item.endIndex())
                            .build())
                .toList())
        .build();
  }
}
