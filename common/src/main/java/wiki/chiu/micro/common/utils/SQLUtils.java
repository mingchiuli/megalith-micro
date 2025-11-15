package wiki.chiu.micro.common.utils;

import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SQLUtils {

    private static final List<Class<?>> NUMBER_TYPES = List.of(
            Long.class, Integer.class, Double.class, Float.class,
            Short.class, Byte.class, BigDecimal.class,
            long.class, int.class, double.class, float.class,
            short.class, byte.class
    );

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static <E> String entityToInsertSQL(List<E> entities, String tableName) {
        if (CollectionUtils.isEmpty(entities)) {
            return "";
        }

        E first = entities.getFirst();
        Field[] fields = getValidFields(first.getClass());

        if (fields.length == 0) {
            return "";
        }

        // 构建 INSERT 语句前缀
        StringBuilder prefix = new StringBuilder();
        prefix.append("INSERT INTO ")
                .append(tableName)
                .append(" (");

        String fieldNames = Arrays.stream(fields)
                .map(field -> toUnderscoreCase(field.getName()))
                .collect(Collectors.joining(", "));

        prefix.append(fieldNames).append(") VALUES ");

        // 构建每行数据
        return entities.stream()
                .map(entity -> {
                    String values = Arrays.stream(fields)
                            .map(field -> formatFieldValue(field, entity))
                            .collect(Collectors.joining(", "));
                    return "(" + values + ")";
                })
                .collect(Collectors.joining(",\n", prefix.toString(), ";"));
    }

    public static String compose(String... sqlList) {
        return String.join("\n", sqlList);
    }

    /**
     * 获取有效字段（排除 static 和 transient）
     */
    private static Field[] getValidFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> !Modifier.isTransient(field.getModifiers()))
                .toArray(Field[]::new);
    }

    /**
     * 格式化字段值为 SQL 格式
     */
    private static String formatFieldValue(Field field, Object entity) {
        try {
            field.setAccessible(true);
            Object value = field.get(entity);

            // 处理 NULL
            if (value == null) {
                return "NULL";
            }

            Class<?> fieldType = field.getType();

            // 数值类型（不加引号）
            if (NUMBER_TYPES.contains(fieldType)) {
                return String.valueOf(value);
            }

            // 布尔类型
            if (fieldType == Boolean.class || fieldType == boolean.class) {
                return (Boolean) value ? "1" : "0";
            }

            // 日期时间类型
            return switch (value) {
                case LocalDateTime localDateTime -> "'" + localDateTime.format(DATE_TIME_FORMATTER) + "'";
                case LocalDate localDate -> "'" + localDate.format(DATE_FORMATTER) + "'";
                case Date date -> "'" + new java.sql.Timestamp(date.getTime()) + "'";
                default ->

                    // 字符串类型（转义特殊字符）
                        "'" + escapeSqlString(String.valueOf(value)) + "'";
            };

        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }

    /**
     * 转义 SQL 字符串中的特殊字符
     */
    private static String escapeSqlString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")    // 反斜杠
                .replace("'", "''")        // 单引号（SQL 标准转义）
                .replace("\n", "\\n")      // 换行符
                .replace("\r", "\\r")      // 回车符
                .replace("\t", "\\t")      // 制表符
                .replace("\0", "\\0");     // NULL 字符
    }

    /**
     * 驼峰命名转下划线命名
     */
    private static String toUnderscoreCase(String camelCaseStr) {
        return camelCaseStr
                .replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .toLowerCase();
    }
}