package wiki.chiu.micro.common.utils;

import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SQLUtils {

    private static final List<Class<? extends Number>> numberClassSet = List.of(Long.class, Integer.class);

    public static <E> String entityToInsertSQL(List<E> entities, String tableName) {
        if (CollectionUtils.isEmpty(entities)) {
            return "";
        }
        E first = entities.getFirst();
        StringBuilder prefix = new StringBuilder();
        prefix
                .append("insert into ")
                .append(tableName)
                .append(" (");

        String fields = Arrays.stream(first.getClass().getDeclaredFields())
                .map(item -> toUnderscoreCase(item.getName()))
                .collect(Collectors.joining(","));

        prefix
                .append(fields)
                .append(") values (");

        return entities.stream()
                .map(item -> {
                    String fieldValues = Arrays.stream(item.getClass().getDeclaredFields())
                            .map(field -> numberClassSet.contains(field.getType()) ? getNumFieldValue(field, item) :  "'" + getFieldValue(field, item) + "'")
                            .collect(Collectors.joining(","));
                    return prefix + fieldValues + ");";
                })
                .collect(Collectors.joining("\n"));
    }

    public static String compose(String... sqlList) {
        return String.join("\n", sqlList);
    }

    private static String getNumFieldValue(Field field, Object item) {
        try {
            field.setAccessible(true);
            //null
            return String.valueOf(field.get(item));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFieldValue(Field field, Object item) {
        try {
            field.setAccessible(true);
            //null
            String rawValue = String.valueOf(field.get(item));
            return rawValue.replaceAll("'", "\\\\'");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toUnderscoreCase(String camelCaseStr) {
        return camelCaseStr
                .replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .toLowerCase();
    }
}
