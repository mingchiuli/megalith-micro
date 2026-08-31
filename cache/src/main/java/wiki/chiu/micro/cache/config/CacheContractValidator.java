package wiki.chiu.micro.cache.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.cache.key.CacheDescriptor;

final class CacheContractValidator implements BeanPostProcessor {

    private final Map<CacheDescriptor, CacheMethod> methods = new ConcurrentHashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanType = ClassUtils.getUserClass(bean);
        Map<Method, Cache> cacheMethods =
            MethodIntrospector.selectMethods(
                beanType,
                (MethodIntrospector.MetadataLookup<Cache>)
                    method -> AnnotatedElementUtils.findMergedAnnotation(method, Cache.class));
        cacheMethods.forEach((method, annotation) -> validate(beanType, method, annotation));
        return bean;
    }

    private void validate(Class<?> beanType, Method method, Cache annotation) {
        int modifiers = method.getModifiers();
        if (Modifier.isPrivate(modifiers)
            || Modifier.isStatic(modifiers)
            || Modifier.isFinal(modifiers)) {
            throw new IllegalStateException("Cache method must be proxyable: " + method);
        }
        if (method.getReturnType() == Void.TYPE) {
            throw new IllegalStateException("Cache method must return a value: " + method);
        }
        if (annotation.ttl() <= 0 || annotation.timeUnit().toNanos(annotation.ttl()) <= 0) {
            throw new IllegalStateException("Cache TTL must be positive on " + method);
        }

        CacheDescriptor descriptor =
            new CacheDescriptor(annotation.namespace(), annotation.version());
        CacheMethod candidate = new CacheMethod(beanType.getName(), method.toGenericString());
        CacheMethod existing = methods.putIfAbsent(descriptor, candidate);
        if (existing != null && !existing.equals(candidate)) {
            throw new IllegalStateException(
                "Cache descriptor "
                    + descriptor
                    + " is already owned by "
                    + existing.description()
                    + "; it cannot also be used by "
                    + candidate.description());
        }
    }

    private record CacheMethod(String beanType, String method) {

        String description() {
            return beanType + "#" + method;
        }
    }
}
