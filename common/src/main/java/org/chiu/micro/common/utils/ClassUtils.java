package org.chiu.micro.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ClassUtils {

    private ClassUtils() {
    }

    public static Class<?>[] findClassArray(Object[] args) {
        var classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (Objects.nonNull(arg)) {
                switch (arg) {
                    case List<?> ignored -> classes[i] = List.class;
                    case Map<?, ?> ignored -> classes[i] = Map.class;
                    case Set<?> ignored -> classes[i] = Set.class;
                    case HttpServletRequest ignored -> classes[i] = HttpServletRequest.class;
                    case HttpServletResponse ignored -> classes[i] = HttpServletResponse.class;
                    default -> classes[i] = arg.getClass();
                }

            }
        }
        return classes;
    }
}
