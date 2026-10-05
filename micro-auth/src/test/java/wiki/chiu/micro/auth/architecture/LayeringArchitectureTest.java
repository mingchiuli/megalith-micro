package wiki.chiu.micro.auth.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.auth.adapter.in.http.AuthInternalHttpHandler;
import wiki.chiu.micro.auth.api.AuthHttpService;
import wiki.chiu.micro.common.arch.ApplicationLayers;

class LayeringArchitectureTest {

    private static final String ROOT = "wiki.chiu.micro.auth";

    @Test
    void applicationLayersAreRespected() {
        ApplicationLayers.verify(ROOT);
    }

    @Test
    void internalHttpHandlerImplementsThePublishedContract() {
        assertThat(AuthHttpService.class).isAssignableFrom(AuthInternalHttpHandler.class);
    }
}
