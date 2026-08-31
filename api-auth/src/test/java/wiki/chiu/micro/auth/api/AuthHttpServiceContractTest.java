package wiki.chiu.micro.auth.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import wiki.chiu.micro.auth.api.req.AuthorityRouteReq;
import wiki.chiu.micro.auth.api.req.WebSocketTicketReq;
import wiki.chiu.micro.common.security.InternalHttpHeaders;

class AuthHttpServiceContractTest {

    @Test
    void endpointsUseTheSharedPostPaths() throws NoSuchMethodException {
        var authorityRoute =
            AuthHttpService.class.getMethod("getAuthorityRoute", AuthorityRouteReq.class, String.class);
        var websocketTicket =
            AuthHttpService.class.getMethod(
                "issueWebSocketTicket", WebSocketTicketReq.class, String.class);

        assertThat(authorityRoute.getAnnotation(PostExchange.class).value())
            .isEqualTo(AuthHttpPaths.AUTH_ROUTE);
        assertThat(websocketTicket.getAnnotation(PostExchange.class).value())
            .isEqualTo(AuthHttpPaths.WEBSOCKET_TOKEN);
    }

    @Test
    void websocketPrincipalHeaderIsOptionalForTheClientInterceptor() throws NoSuchMethodException {
        var method =
            AuthHttpService.class.getMethod(
                "issueWebSocketTicket", WebSocketTicketReq.class, String.class);
        RequestHeader header = method.getParameters()[1].getAnnotation(RequestHeader.class);

        assertThat(header.value()).isEqualTo(InternalHttpHeaders.PRINCIPAL);
        assertThat(header.required()).isFalse();
    }

    @Test
    void httpServiceDoesNotDeclareDefaultMethods() {
        assertThat(AuthHttpService.class.getDeclaredMethods()).noneMatch(method -> method.isDefault());
    }

    @Test
    void websocketTicketUsesTheHttpExchangeMethodDirectly() {
        var restClientBuilder =
            RestClient.builder()
                .baseUrl("https://auth.test")
                .configureMessageConverters(
                    converters -> converters.withJsonConverter(new JacksonJsonHttpMessageConverter()));
        var server = MockRestServiceServer.bindTo(restClientBuilder).build();
        var client =
            HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClientBuilder.build()))
                .build()
                .createClient(AuthHttpService.class);

        server
            .expect(requestTo("https://auth.test" + AuthHttpPaths.WEBSOCKET_TOKEN))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("{\"roomId\":\"blog-7\"}"))
            .andRespond(
                withSuccess(
                    "{\"msg\":\"success\",\"code\":200,\"data\":\"ticket\"}",
                    MediaType.APPLICATION_JSON));

        assertThat(client.issueWebSocketTicket(new WebSocketTicketReq("blog-7"), null).data())
            .isEqualTo("ticket");
        server.verify();
    }
}
