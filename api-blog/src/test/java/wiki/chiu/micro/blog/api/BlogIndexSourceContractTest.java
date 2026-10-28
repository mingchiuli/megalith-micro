package wiki.chiu.micro.blog.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

class BlogIndexSourceContractTest {

    @Test
    void maintenanceSourceExposesStatusAndCursorSnapshots() throws Exception {
        assertThat(BlogIndexSourceHttpService.class.getMethod("indexSourceStatus")
            .getAnnotation(GetExchange.class).value()).isEqualTo("/blog/index/status");
        assertThat(BlogIndexSourceHttpService.class.getMethod("indexSnapshots", long.class, int.class)
            .getAnnotation(GetExchange.class).value()).isEqualTo("/blog/index/snapshots");
        assertThat(BlogIndexSourceHttpService.class.getMethod(
            "indexBootstrapSnapshots", long.class, int.class)
            .getAnnotation(GetExchange.class).value()).isEqualTo("/blog/index/bootstrap-snapshots");
        assertThat(BlogIndexSourceHttpService.class.getMethod(
            "indexAdminSnapshots", long.class, int.class)
            .getAnnotation(GetExchange.class).value()).isEqualTo("/blog/index/admin-snapshots");
        assertThat(BlogIndexSourceHttpService.class.getMethod(
            "indexRevisionSnapshots", long.class, int.class)
            .getAnnotation(GetExchange.class).value()).isEqualTo("/blog/index/revision-snapshots");
        assertThat(BlogIndexSourceHttpService.class.getMethod(
            "indexRevisionSnapshots", long.class, int.class)
            .getParameters()[0].getAnnotation(RequestParam.class).value())
            .isEqualTo("afterRevisionId");
    }
}
