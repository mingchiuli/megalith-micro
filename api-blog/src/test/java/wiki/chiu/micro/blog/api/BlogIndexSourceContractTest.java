package wiki.chiu.micro.blog.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.service.annotation.GetExchange;

class BlogIndexSourceContractTest {

    @Test
    void maintenanceSourceExposesStatusAndCursorSnapshots() throws Exception {
        assertThat(BlogIndexSourceHttpService.class.getMethod("indexSourceStatus")
            .getAnnotation(GetExchange.class).value()).isEqualTo("/blog/index/status");
        assertThat(BlogIndexSourceHttpService.class.getMethod("indexSnapshots", long.class, int.class)
            .getAnnotation(GetExchange.class).value()).isEqualTo("/blog/index/snapshots");
    }
}
