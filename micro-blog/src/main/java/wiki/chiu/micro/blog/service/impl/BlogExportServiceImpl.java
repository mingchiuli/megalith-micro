package wiki.chiu.micro.blog.service.impl;

import static wiki.chiu.micro.common.lang.Const.BLOG_SENSITIVE_TABLE;
import static wiki.chiu.micro.common.lang.Const.BLOG_TABLE;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.blog.convertor.BlogSysCountSearchReqConvertor;
import wiki.chiu.micro.blog.convertor.BlogSysSearchReqConvertor;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.entity.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.req.BlogDownloadReq;
import wiki.chiu.micro.blog.service.BlogExportService;
import wiki.chiu.micro.blog.service.port.BlogSearchGateway;
import wiki.chiu.micro.common.utils.SQLUtils;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;

@Service
public class BlogExportServiceImpl implements BlogExportService {

  private static final int PAGE_SIZE = 20;

  private final BlogRepository blogs;
  private final BlogSensitiveContentRepository sensitiveContent;
  private final BlogSearchGateway search;

  public BlogExportServiceImpl(
      BlogRepository blogs,
      BlogSensitiveContentRepository sensitiveContent,
      BlogSearchGateway search) {
    this.blogs = blogs;
    this.sensitiveContent = sensitiveContent;
    this.search = search;
  }

  @Override
  public void write(
      BlogDownloadReq request, Long userId, List<String> roles, OutputStream outputStream) {
    BlogSysCountSearchReq countRequest =
        BlogSysCountSearchReqConvertor.convert(request, userId, roles);
    long total = search.countBlogs(countRequest);
    long pageCount = (total + PAGE_SIZE - 1) / PAGE_SIZE;

    try {
      OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
      for (int page = 1; page <= pageCount; page++) {
        BlogSysSearchReq searchRequest =
            BlogSysSearchReqConvertor.convert(request, page, PAGE_SIZE, userId, roles);
        writePage(search.searchBlogs(searchRequest), writer);
      }
      writer.flush();
    } catch (IOException exception) {
      throw new IllegalStateException("failed to write blog export", exception);
    }
  }

  private void writePage(BlogSearchRpcVo result, OutputStreamWriter writer) throws IOException {
    List<Long> ids = result.ids();
    if (ids.isEmpty()) {
      return;
    }
    Map<Long, Integer> order = new HashMap<>();
    for (int index = 0; index < ids.size(); index++) {
      order.put(ids.get(index), index);
    }
    List<BlogEntity> pageBlogs =
        blogs.findAllById(ids).stream()
            .sorted(
                (left, right) ->
                    Integer.compare(
                        order.getOrDefault(left.getId(), Integer.MAX_VALUE),
                        order.getOrDefault(right.getId(), Integer.MAX_VALUE)))
            .toList();
    List<BlogSensitiveContentEntity> pageSensitive = sensitiveContent.findByBlogIdIn(ids);
    writeStatement(writer, SQLUtils.entityToInsertSQL(pageBlogs, BLOG_TABLE));
    writeStatement(writer, SQLUtils.entityToInsertSQL(pageSensitive, BLOG_SENSITIVE_TABLE));
  }

  private static void writeStatement(OutputStreamWriter writer, String statement)
      throws IOException {
    if (!statement.isBlank()) {
      writer.write(statement);
      writer.write(System.lineSeparator());
    }
  }
}
