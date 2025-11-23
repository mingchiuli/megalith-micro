package wiki.chiu.micro.cache.aot.hints;

import wiki.chiu.micro.cache.listener.RabbitCacheEvictMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.ReflectionUtils.findMethod;

class CacheRuntimeHints implements RuntimeHintsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(CacheRuntimeHints.class);

    @SuppressWarnings("all")
    @Override
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        try {
            // 1. 注册RabbitMQ消息处理方法
            hints.reflection()
                    .registerMethod(
                            findMethod(RabbitCacheEvictMessageListener.class, "handleMessage", Set.class),
                            ExecutableMode.INVOKE
                    );

            // 2. 注册HashSet构造函数
            hints.reflection()
                    .registerConstructor(HashSet.class.getDeclaredConstructor(), ExecutableMode.INVOKE);

            // 3. 注册SSMSA类及其关键成员（针对static final字段和VarHandle访问）
            TypeReference ssmsaType = TypeReference.of("com.github.benmanes.caffeine.cache.SSMSA");
            hints.reflection()
                    // 注册类及基础成员类别
                    .registerType(ssmsaType,
                            MemberCategory.ACCESS_DECLARED_FIELDS,
                            MemberCategory.ACCESS_PUBLIC_FIELDS,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS)
                    // 显式注册static final的FACTORY字段
                    .registerType(ssmsaType, typeHint -> {
                        typeHint.withField("FACTORY"); // 关键：显式声明FACTORY字段
                        typeHint.withField("EXPIRES_AFTER_ACCESS_NANOS"); // VarHandle访问的静态字段
                    });


        } catch (Exception e) {
            log.error("Failed to register runtime hints", e);
            throw new RuntimeException("Application start failed: " + e.getMessage(), e);
        }
    }

}