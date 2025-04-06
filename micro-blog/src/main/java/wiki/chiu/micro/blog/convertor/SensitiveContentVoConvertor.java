package wiki.chiu.micro.blog.convertor;

import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.common.vo.SensitiveContentRpcVo;

import java.util.List;

public class SensitiveContentVoConvertor {


    public static List<BlogEditVo.SensitiveContentVo> convert(List<SensitiveContentRpcVo> sensitiveContentRpcList) {
        return sensitiveContentRpcList.stream()
                .map(item -> BlogEditVo.SensitiveContentVo.builder()
                        .type(item.type())
                        .startIndex(item.startIndex())
                        .endIndex(item.endIndex())
                        .build())
                .toList();
    }
}
