package wiki.chiu.micro.auth.adapter.in.http;

import java.util.List;

public record MenuWithChildVo(
    Long id,
    Long parentId,
    String title,
    String name,
    String url,
    String component,
    Integer type,
    String icon,
    Integer orderNum,
    Integer status,
    List<MenuWithChildVo> children) {
}
