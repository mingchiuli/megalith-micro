package wiki.chiu.micro.blog.convertor;

import java.util.List;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.vo.BlogEditVo;

public class SensitiveContentVoConvertor {

  public static List<BlogEditVo.SensitiveContentVo> convert(
      List<BlogSensitiveContentEntity> sensitiveContentList) {
    return sensitiveContentList.stream()
        .map(
            item ->
                BlogEditVo.SensitiveContentVo.builder()
                    .type(item.getType())
                    .startIndex(item.getStartIndex())
                    .endIndex(item.getEndIndex())
                    .build())
        .toList();
  }
}
