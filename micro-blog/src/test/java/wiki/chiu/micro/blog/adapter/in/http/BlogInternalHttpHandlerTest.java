package wiki.chiu.micro.blog.adapter.in.http;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.blog.application.port.in.BlogSensitiveService;
import wiki.chiu.micro.common.web.ValidatedRequest;

class BlogInternalHttpHandlerTest {

    private final BlogQueryService blogs = mock(BlogQueryService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BlogInternalHttpHandler handler =
            new BlogInternalHttpHandler(
                blogs, mock(BlogSensitiveService.class), new ValidatedRequest());
        mockMvc =
            MockMvcBuilders.routerFunctions(BlogRoutes.routes(mock(BlogHttpHandler.class), handler))
                .build();
    }

    @Test
    void returnsIdsAfterValidatedCursor() throws Exception {
        when(blogs.findIdsAfter(0L, 1000)).thenReturn(List.of(1L, 4L));

        mockMvc
            .perform(get("/inner/blog/ids").param("afterId", "0").param("limit", "1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0]").value(1))
            .andExpect(jsonPath("$.data[1]").value(4));

        verify(blogs).findIdsAfter(0L, 1000);
    }

    @Test
    void rejectsOversizedBatch() throws Exception {
        mockMvc
            .perform(get("/inner/blog/ids").param("afterId", "0").param("limit", "1001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("limit must be between 1 and 1000"));
    }
}
