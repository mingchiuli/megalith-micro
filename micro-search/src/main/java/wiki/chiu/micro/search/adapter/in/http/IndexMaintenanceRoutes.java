package wiki.chiu.micro.search.adapter.in.http;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static wiki.chiu.micro.common.web.FunctionalWeb.withDefaultErrorHandling;

import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.search.api.vo.IndexRebuildRpcVo;

@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding(IndexRebuildRpcVo.class)
public class IndexMaintenanceRoutes {

    @Bean
    RouterFunction<ServerResponse> indexMaintenanceRouter(IndexMaintenanceHttpHandler handler) {
        return withDefaultErrorHandling(route().POST("/inner/search/index/rebuild", handler::rebuild),
            LoggerFactory.getLogger(IndexMaintenanceRoutes.class)).build();
    }
}
