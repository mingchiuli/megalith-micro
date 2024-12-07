package wiki.chiu.micro.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import wiki.chiu.micro.common.exception.MissException;

public class JsonUtils {

    public static  <T> T readValue(ObjectMapper objectMapper, String str, Class<T> clazz) {
        try {
            return objectMapper.readValue(str, clazz);
        } catch (JsonProcessingException e) {
            throw new MissException(e.getMessage());
        }
    }

    public static <T> T convertValue(ObjectMapper objectMapper, Object obj, Class<T> clazz) {
        return objectMapper.convertValue(obj, clazz);
    }

    public static String writeValueAsString(ObjectMapper objectMapper, Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new MissException(e.getMessage());
        }
    }

    public static  <T>T readValue(ObjectMapper objectMapper, String str, TypeReference<T> type) {
        try {
            return objectMapper.readValue(str, type);
        } catch (JsonProcessingException e) {
            throw new MissException(e.getMessage());
        }
    }

    public static Object readValue(ObjectMapper objectMapper, String remoteCacheStr, JavaType javaType) {
        try {
            return objectMapper.readValue(remoteCacheStr, javaType);
        } catch (JsonProcessingException e) {
            throw new MissException(e.getMessage());
        }
    }
}
