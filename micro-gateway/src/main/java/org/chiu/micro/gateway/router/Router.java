package org.chiu.micro.gateway.router;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.chiu.micro.gateway.dto.AuthorityRouteDto;
import org.chiu.micro.gateway.lang.ExceptionMessage;
import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.req.AuthorityRouteReq;
import org.chiu.micro.gateway.rpc.wrapper.AuthHttpServiceWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequiredArgsConstructor
@Slf4j
public class Router {

    private final RestClient restClient;

    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    private final ObjectMapper objectMapper;
    
    @RequestMapping(value = "/**", method = { RequestMethod.GET, RequestMethod.POST })
    @SneakyThrows
    public void dispatch(HttpServletRequest request, HttpServletResponse response) {

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String ipAddress = getIpAddr(request);
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        String authorization = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).orElse("");

        AuthorityRouteDto authorityRoute = authHttpServiceWrapper.getAuthorityRoute(AuthorityRouteReq.builder()
                        .routeMapping(requestURI)
                        .method(method)
                        .token(authorization)
                        .ipAddr(ipAddress)
                        .build());

        if (Boolean.FALSE.equals(authorityRoute.getAuth())) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            Result.fail(ExceptionMessage.NO_FOUND.getMsg())));
            return;
        }

        String serviceHost = authorityRoute.getServiceHost();
        Integer servicePort = authorityRoute.getServicePort();

        String url = "http://" + serviceHost + ":" + servicePort + requestURI;

        HttpMethod httpMethod = HttpMethod.valueOf(method);
        Map<String, String[]> parameterMap = request.getParameterMap();
        ResponseEntity<byte[]> responseEntity = null;

        if (HttpMethod.POST.equals(httpMethod)) {
            Object body;
            MediaType contenType;
            if (request instanceof MultipartHttpServletRequest req) {
                // upload / login request
                body = req.getFileMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getResource()));
                contenType = MediaType.MULTIPART_FORM_DATA;
            } else {
                body = request.getInputStream().readAllBytes();
                contenType = MediaType.APPLICATION_JSON;
            }
            responseEntity = restClient
                    .post()
                    .uri(url, uriBuilder -> {
                        parameterMap.entrySet()
                                .forEach(entry -> uriBuilder.queryParam(entry.getKey(), List.of(entry.getValue())));
                        return uriBuilder.build();
                    })
                    .contentType(contenType)
                    .body(body)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
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
                        parameterMap.entrySet()
                                .forEach(entry -> uriBuilder.queryParam(entry.getKey(), List.of(entry.getValue())));
                        return uriBuilder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
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
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }
    
    private String getIpAddr(HttpServletRequest request) {
        // nginx代理获取的真实用户ip
        String ip = request.getHeader("X-Real-IP");
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
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
