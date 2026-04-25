package wiki.chiu.micro.blog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wiki.chiu.micro.blog.exception.GlobalExceptionHandler;
import wiki.chiu.micro.blog.service.BlogService;
import wiki.chiu.micro.blog.support.StubAuthInfoResolver;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.page.PageAdapter;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

    @Mock
    private BlogService blogService;

    @InjectMocks
    private BlogController blogController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(blogController)
                .setCustomArgumentResolvers(new StubAuthInfoResolver(1L, List.of("ROLE_USER")))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void saveOrUpdateReturnsSuccess() throws Exception {
        doNothing().when(blogService).saveOrUpdate(any(), anyLong(), anyList());

        String body = "{\"id\":null,\"title\":\"t\",\"description\":\"d\",\"content\":\"c\","
                + "\"status\":0,\"link\":\"\",\"sensitiveContentList\":[]}";
        mockMvc.perform(post("/sys/blog/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(blogService).saveOrUpdate(any(), anyLong(), anyList());
    }

    @Test
    void deleteBlogsReturnsSuccess() throws Exception {
        doNothing().when(blogService).deleteBatch(anyList(), anyLong(), anyList());

        mockMvc.perform(post("/sys/blog/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void setBlogTokenReturnsToken() throws Exception {
        when(blogService.setBlogToken(7L, 1L)).thenReturn("xyz-token");

        mockMvc.perform(get("/sys/blog/lock/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("xyz-token"));
    }

    @Test
    void getAllBlogsReturnsPage() throws Exception {
        BlogEntityVo vo = BlogEntityVo.builder().id(1L).title("t").build();
        PageAdapter<BlogEntityVo> page = PageAdapter.<BlogEntityVo>builder()
                .content(List.of(vo)).totalElements(1).pageNumber(1).pageSize(10)
                .first(true).last(true).empty(false).totalPages(1).build();
        when(blogService.findAllBlogs(any(), anyLong(), anyList())).thenReturn(page);

        mockMvc.perform(get("/sys/blog/blogs")
                        .param("currentPage", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void getDeletedBlogsReturnsPage() throws Exception {
        when(blogService.findDeletedBlogs(1, 10, 1L)).thenReturn(PageAdapter.emptyPage());

        mockMvc.perform(get("/sys/blog/deleted")
                        .param("currentPage", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.empty").value(true));
    }

    @Test
    void recoverDeletedBlogReturnsSuccess() throws Exception {
        doNothing().when(blogService).recoverDeletedBlog(2, 1L);

        mockMvc.perform(get("/sys/blog/recover/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void uploadOssReturnsUrl() throws Exception {
        when(blogService.uploadOss(any(), anyLong())).thenReturn("https://oss/x.png");
        MockMultipartFile file = new MockMultipartFile("image", "x.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/sys/blog/oss/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("https://oss/x.png"));
    }

    @Test
    void deleteOssReturnsSuccess() throws Exception {
        doNothing().when(blogService).deleteOss("https://oss/x.png");

        mockMvc.perform(get("/sys/blog/oss/delete").param("url", "https://oss/x.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getEchoDetailWhenServiceThrowsReturns400() throws Exception {
        when(blogService.findEdit(any(), anyLong(), anyList()))
                .thenThrow(new MissException("not found"));

        mockMvc.perform(get("/sys/blog/edit/pull/echo").param("blogId", "9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("not found"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/sys/blog/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBlogsWithEmptyListShouldFailIfValidationActive() throws Exception {
        doThrow(new IllegalArgumentException("ids empty")).when(blogService)
                .deleteBatch(anyList(), anyLong(), anyList());

        mockMvc.perform(post("/sys/blog/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }
}
