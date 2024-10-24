package org.chiu.micro.exhibit.bloom;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.chiu.micro.common.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-06-07 11:01 AM
 */
@Aspect
@Component
@Order(1)
public class BloomAspect {

    private static final Logger log = LoggerFactory.getLogger(BloomAspect.class);
    private final List<BloomHandler> bloomHandlers;

    public BloomAspect(List<BloomHandler> bloomHandlers) {
        this.bloomHandlers = bloomHandlers;
    }

    @Pointcut("@annotation(org.chiu.micro.exhibit.bloom.Bloom)")
    public void pt() {
    }

    @Before("pt()")
    public void before(JoinPoint jp) throws NoSuchMethodException {
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
