package org.chiu.micro.exhibit.bloom;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.chiu.micro.exhibit.utils.ClassUtils;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author mingchiuli
 * @create 2022-06-07 11:01 AM
 */
@Aspect
@Component
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class BloomAspect {

    private final List<BloomHandler> bloomHandlers;

    @Pointcut("@annotation(org.chiu.micro.exhibit.bloom.Bloom)")
    public void pt() {}

    @SneakyThrows
    @Before("pt()")
    public void before(JoinPoint jp) {
        Signature signature = jp.getSignature();
        //方法名
        String methodName = signature.getName();
        //参数
        Object[] args = jp.getArgs();
        Class<?>[] classes = ClassUtils.findClassArray(args);

        Class<?> declaringType = signature.getDeclaringType();
        Method method = declaringType.getMethod(methodName, classes);
        Bloom bloom = method.getAnnotation(Bloom.class);
        Class<? extends BloomHandler> handler0 = bloom.handler();

        for (BloomHandler handler : bloomHandlers) {
            if (handler.supports(handler0)) {
                try {
                    handler.handle(args);
                    break;
                } catch (NestedRuntimeException e) {
                    log.error(e.toString());
                }
            }
        }
    }
}
