package org.chiu.micro.user.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author mingchiuli
 * @create 2021-12-22 12:58 AM
 */

@Component(value = "springUtils")
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public synchronized void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

    public static <T> List<T> getBeans(Class<T> clazz){
        Map<String, T> beansOfType = applicationContext.getBeansOfType(clazz);
        return new ArrayList<>(beansOfType.values());
    }

    public static <T> Map<String, T> getHandlers(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz);
    }
}
