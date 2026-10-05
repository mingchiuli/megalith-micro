package wiki.chiu.micro.exhibit.architecture;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.arch.ApplicationLayers;

class LayeringArchitectureTest {

    private static final String ROOT = "wiki.chiu.micro.exhibit";

    @Test
    void applicationLayersAreRespected() {
        ApplicationLayers.verify(ROOT);
    }
}
