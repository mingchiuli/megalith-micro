package wiki.chiu.micro.cache.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.annotation.Order;
import wiki.chiu.micro.cache.annotation.Checker;
import wiki.chiu.micro.cache.handler.CheckerHandler;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author mingchiuli
 * @since 2022-06-07 11:01 AM
 */
@Aspect
@Order(1)
public class CheckerAspect {

    private static final Logger log = LoggerFactory.getLogger(CheckerAspect.class);
    private final List<CheckerHandler> checkerHandlers;

    public CheckerAspect(List<CheckerHandler> checkerHandlers) {
        this.checkerHandlers = checkerHandlers;
    }

    @Pointcut("@annotation(wiki.chiu.micro.cache.annotation.Checker)")
    public void pt() {
    }

    @Before("pt()")
    public void before(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        Object[] args = jp.getArgs();
        Checker checker = method.getAnnotation(Checker.class);
        Class<? extends CheckerHandler> handler0 = checker.handler();

        for (CheckerHandler handler : checkerHandlers) {
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
