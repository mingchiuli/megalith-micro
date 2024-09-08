package org.chiu.micro.gateway.router;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import org.springframework.web.multipart.MultipartFile;
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
import java.util.Optional;

import org.chiu.micro.gateway.dto.AuthorityRouteDto;
import org.chiu.micro.gateway.lang.Const;
import org.chiu.micro.gateway.lang.ExceptionMessage;
import org.chiu.micro.gateway.lang.Result;
import org.chiu.micro.gateway.req.AuthorityRouteReq;
import org.chiu.micro.gateway.rpc.wrapper.AuthHttpServiceWrapper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

        String method = request.getMethod();
        String requestURI = request.getRequestURI();

        AuthorityRouteDto authorityRoute = authHttpServiceWrapper.getAuthorityRoute(
                AuthorityRouteReq.builder()
                        .routeMapping(requestURI)
                        .method(method)
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
        String authorization = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).orElse("");

        String url = Const.PROTOFILE.getInfo() + serviceHost + Const.PROTOFILE_SPLIT.getInfo() + servicePort + requestURI;
        log.info("url:{}", url);

        HttpMethod httpMethod = HttpMethod.valueOf(method);
        Map<String, String[]> parameterMap = request.getParameterMap();
        ResponseEntity<byte[]> responseEntity = null;

        if (HttpMethod.POST.equals(httpMethod)) {
            // upload request
            if (request instanceof MultipartHttpServletRequest req) {
                Map<String, MultipartFile> fileMap = req.getFileMap();
                MultiValueMap<String, Resource> parts = new LinkedMultiValueMap<>();
                fileMap.entrySet().forEach(entry -> parts.add(entry.getKey(), entry.getValue().getResource()));
                responseEntity = restClient
                        .post()
                        .uri(url)
                        .body(parts)
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .retrieve()
                        .toEntity(byte[].class);
            } else {
                log.info("parameterMap:{}", parameterMap);
                log.info("parameterMap:{}", parameterMap);
                responseEntity = restClient
                        .post()
                        .uri(url, uriBuilder -> {
                            parameterMap.entrySet().forEach(entry -> uriBuilder.queryParam(entry.getKey(), List.of(entry.getValue())));
                            return uriBuilder.build();
                        })
                        .body(request.getInputStream().readAllBytes())
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .retrieve()
                        .toEntity(byte[].class);
            }
        }

        if (HttpMethod.GET.equals(httpMethod)) {
            responseEntity = restClient
                    .get()
                    .uri(url, uriBuilder -> {
                        parameterMap.entrySet().forEach(entry -> uriBuilder.queryParam(entry.getKey(), List.of(entry.getValue())));
                        return uriBuilder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toEntity(byte[].class);
        }

        if (responseEntity == null || responseEntity.getBody() == null) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            Result.fail(ExceptionMessage.DATA_NOT_FOUND.getMsg())));
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
        var outputStream = response.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }

}
