package wiki.chiu.micro.gateway.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import wiki.chiu.micro.common.dto.AuthorityRouteRpcDto;
import wiki.chiu.micro.common.lang.ExceptionMessage;
import wiki.chiu.micro.common.lang.Result;

import wiki.chiu.micro.common.req.AuthorityRouteReq;
import wiki.chiu.micro.gateway.rpc.AuthHttpServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class Router {

    private static final Logger log = LoggerFactory.getLogger(Router.class);
    private final RestClient restClient;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    private final ObjectMapper objectMapper;

    private static final String UNKNOWN = "unknown";

    public Router(RestClient restClient, AuthHttpServiceWrapper authHttpServiceWrapper, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.authHttpServiceWrapper = authHttpServiceWrapper;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST})
    public void dispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String ipAddress = getIpAddr(request);
        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        AuthorityRouteRpcDto authorityRoute = authHttpServiceWrapper.getAuthorityRoute(AuthorityRouteReq.builder()
                .routeMapping(requestURI)
                .method(method)
                .ipAddr(ipAddress)
                .build());

        if (Boolean.FALSE.equals(authorityRoute.auth())) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            Result.fail(ExceptionMessage.RE_LOGIN.getMsg())));
            return;
        }

        String serviceHost = authorityRoute.serviceHost();
        Integer servicePort = authorityRoute.servicePort();

        String url = "http://" + serviceHost + ":" + servicePort + requestURI;

        HttpMethod httpMethod = HttpMethod.valueOf(method);
        Map<String, String[]> parameterMap = request.getParameterMap();
        ResponseEntity<byte[]> responseEntity = null;

        if (HttpMethod.POST.equals(httpMethod)) {
            Object body;
            MediaType contentType;
            if (request instanceof MultipartHttpServletRequest req) {
                // upload / login request
                MultiValueMap<String, Resource> map = new LinkedMultiValueMap<>();
                req.getFileMap().forEach((key, value) -> map.add(key, value.getResource()));
                body = map;
                contentType = MediaType.MULTIPART_FORM_DATA;
            } else {
                body = request.getInputStream().readAllBytes();
                contentType = MediaType.APPLICATION_JSON;
            }
            responseEntity = restClient
                    .post()
                    .uri(url, uriBuilder -> {
                        parameterMap.forEach((key, value) -> uriBuilder.queryParam(key, List.of(value)));
                        return uriBuilder.build();
                    })
                    .contentType(contentType)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (_, resp) -> {
                        HttpStatusCode statusCode = resp.getStatusCode();
                        byte[] respBody = resp.getBody().readAllBytes();
                        response.setStatus(statusCode.value());
                        response.getOutputStream().write(respBody);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    })
                    .toEntity(byte[].class);
        }

        if (HttpMethod.GET.equals(httpMethod)) {
            responseEntity = restClient
                    .get()
                    .uri(url, uriBuilder -> {
                        parameterMap.forEach((key, value) -> uriBuilder.queryParam(key, List.of(value)));
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (_, resp) -> {
                        HttpStatusCode statusCode = resp.getStatusCode();
                        byte[] respBody = resp.getBody().readAllBytes();
                        response.setStatus(statusCode.value());
                        response.getOutputStream().write(respBody);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    })
                    .toEntity(byte[].class);
        }

        if (response.getStatus() != HttpStatus.OK.value() || responseEntity == null) {
            return;
        }

        HttpHeaders respHeaders = responseEntity.getHeaders();
        List<String> contentTypes = respHeaders.getOrDefault(HttpHeaders.CONTENT_TYPE, Collections.emptyList());

        if (contentTypes.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        }

        byte[] data = responseEntity.getBody();
        response.setContentLength(data == null ? 0 : data.length);
        response.setStatus(responseEntity.getStatusCode().value());

        var outputStream = response.getOutputStream();
        outputStream.write(data == null ? "".getBytes() : data);
        outputStream.flush();
        outputStream.close();
    }

    private String getIpAddr(HttpServletRequest request) {
        // nginx代理获取的真实用户ip
        String ip = request.getHeader("X-Real-IP");
        if (!StringUtils.hasLength(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (!StringUtils.hasLength(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasLength(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasLength(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        /*
          对于通过多个代理的情况， 第一个IP为客户端真实IP,多个IP按照','分割 "***.***.***.***".length() =
          15
         */
        if (Objects.nonNull(ip) && ip.length() > 15) {
            int idx = ip.indexOf(",");
            if (idx > 0) {
                ip = ip.substring(0, idx);
            }
        }
        return ip;
    }

}
