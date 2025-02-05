package wiki.chiu.micro.gateway.router;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.client.ClientHttpResponse;
import wiki.chiu.micro.common.dto.AuthorityRouteRpcDto;

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
    private static final String UNKNOWN = "unknown";
    private static final String AUTH_HEADER = "X-Real-IP";
    private static final String FORWARDED_HEADER = "X-Forwarded-For";
    private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";

    private final RestClient restClient;
    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    public Router(RestClient restClient, AuthHttpServiceWrapper authHttpServiceWrapper) {
        this.restClient = restClient;
        this.authHttpServiceWrapper = authHttpServiceWrapper;
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST})
    public void dispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String ipAddress = getIpAddr(request);
        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        AuthorityRouteRpcDto authorityRoute = fetchRoute(ipAddress, method, requestURI);
        String url = buildUrl(authorityRoute, requestURI);
        ResponseEntity<byte[]> responseEntity = handleRequest(request, url, method, response);

        if (response.getStatus() != HttpStatus.OK.value() || responseEntity == null) {
            return;
        }

        handleResponse(response, responseEntity);
    }

    private AuthorityRouteRpcDto fetchRoute(String ipAddress, String method, String requestURI) {
        AuthorityRouteReq routeReq = AuthorityRouteReq.builder()
                .routeMapping(requestURI)
                .method(method)
                .ipAddr(ipAddress)
                .build();
        return authHttpServiceWrapper.getAuthorityRoute(routeReq);
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasLength(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(FORWARDED_HEADER);
        }
        if (!StringUtils.hasLength(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(PROXY_CLIENT_IP);
        }
        if (!StringUtils.hasLength(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(WL_PROXY_CLIENT_IP);
        }
        if (!StringUtils.hasLength(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (Objects.nonNull(ip) && ip.length() > 15) {
            int idx = ip.indexOf(",");
            if (idx > 0) {
                ip = ip.substring(0, idx);
            }
        }
        return ip;
    }

    private String buildUrl(AuthorityRouteRpcDto authorityRoute, String requestURI) {
        return "http://" + authorityRoute.serviceHost() + ":" + authorityRoute.servicePort() + requestURI;
    }

    private ResponseEntity<byte[]> handleRequest(HttpServletRequest request, String url, String method, HttpServletResponse response) throws IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        if (HttpMethod.POST.equals(httpMethod)) {
            return handlePostRequest(request, url, parameterMap, response);
        } else if (HttpMethod.GET.equals(httpMethod)) {
            return handleGetRequest(url, parameterMap, response);
        }
        return null;
    }

    private ResponseEntity<byte[]> handlePostRequest(HttpServletRequest request, String url, Map<String, String[]> parameterMap, HttpServletResponse response) throws IOException {
        Object body;
        MediaType contentType;
        if (request instanceof MultipartHttpServletRequest req) {
            MultiValueMap<String, Resource> map = new LinkedMultiValueMap<>();
            req.getFileMap().forEach((key, value) -> map.add(key, value.getResource()));
            body = map;
            contentType = MediaType.MULTIPART_FORM_DATA;
        } else {
            body = request.getInputStream().readAllBytes();
            contentType = MediaType.APPLICATION_JSON;
        }
        return restClient
                .post()
                .uri(url, uriBuilder -> {
                    parameterMap.forEach((key, value) -> uriBuilder.queryParam(key, List.of(value)));
                    return uriBuilder.build();
                })
                .contentType(contentType)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (_, resp) -> handleErrorResponse(response, resp))
                .toEntity(byte[].class);
    }

    private ResponseEntity<byte[]> handleGetRequest(String url, Map<String, String[]> parameterMap, HttpServletResponse response) {
        return restClient
                .get()
                .uri(url, uriBuilder -> {
                    parameterMap.forEach((key, value) -> uriBuilder.queryParam(key, List.of(value)));
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, (_, resp) -> handleErrorResponse(response, resp))
                .toEntity(byte[].class);
    }

    private void handleErrorResponse(HttpServletResponse response, ClientHttpResponse resp) throws IOException {
        HttpStatusCode statusCode = resp.getStatusCode();
        byte[] respBody = resp.getBody().readAllBytes();
        response.setStatus(statusCode.value());
        response.getOutputStream().write(respBody);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    private void handleResponse(HttpServletResponse response, ResponseEntity<byte[]> responseEntity) throws IOException {
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
}