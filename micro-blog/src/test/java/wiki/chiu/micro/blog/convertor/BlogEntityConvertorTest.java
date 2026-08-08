package wiki.chiu.micro.blog.convertor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import wiki.chiu.micro.blog.dto.BlogDeleteDto;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

class BlogEntityConvertorTest {

  @Test
  void recoveryPreservesStatusStoredWithDeletedBlog() {
    BlogDeleteDto deleted = BlogDeleteDto.builder().status(BlogStatusEnum.DRAFT.getCode()).build();

    assertEquals(
        BlogStatusEnum.DRAFT.getCode(), BlogEntityConvertor.convertRecover(deleted).getStatus());
  }
}
