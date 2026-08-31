package wiki.chiu.micro.exhibit.convertor;

import static wiki.chiu.micro.common.lang.BlogStatusEnum.HIDE;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.exhibit.application.model.BlogScore;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;

public class BlogHotReadVoConvertor {

    private BlogHotReadVoConvertor() {
    }

    private static final String UNKNOWN_TITLE = "未知标题";

    public static List<BlogHotReadVo> convert(
        List<BlogEntityRpcVo> blogs, List<BlogScore> scores) {
        Map<Long, String> idTitleMap = createIdTitleMap(blogs);
        List<Long> ids = filterVisibleBlogIds(blogs);

        return scores.stream()
            .filter(item -> ids.contains(item.blogId()))
            .map(item -> createBlogHotReadVo(item, idTitleMap))
            .toList();
    }

    private static Map<Long, String> createIdTitleMap(List<BlogEntityRpcVo> blogs) {
        return blogs.stream().collect(Collectors.toMap(BlogEntityRpcVo::id, BlogEntityRpcVo::title));
    }

    private static List<Long> filterVisibleBlogIds(List<BlogEntityRpcVo> blogs) {
        return blogs.stream()
            .filter(item -> !HIDE.getCode().equals(item.status()))
            .map(BlogEntityRpcVo::id)
            .toList();
    }

    private static BlogHotReadVo createBlogHotReadVo(
        BlogScore item, Map<Long, String> idTitleMap) {
        Long id = item.blogId();
        Long readCount = item.readCount();
        String title = idTitleMap.getOrDefault(id, UNKNOWN_TITLE);

        return BlogHotReadVo.builder().id(id).readCount(readCount).title(title).build();
    }
}
