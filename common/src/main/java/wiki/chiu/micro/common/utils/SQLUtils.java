package wiki.chiu.micro.common.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SQLUtils {

    private static final List<Class<? extends Number>> numberClassSet = List.of(Long.class, Integer.class);

        public static <E> String entityToInsertSQL(List<E> entities, String tableName) {
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
                            .map(field -> numberClassSet.contains(field.getType()) ? getFieldValue(field, item) :  "'" + getFieldValue(field, item) + "'")
                            .collect(Collectors.joining(","));
                    return prefix + fieldValues + ");";
                })
                .collect(Collectors.joining("\n"));
    }

    private static String getFieldValue(Field field, Object item) {
        try {
            field.setAccessible(true);
            return field.get(item).toString();
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
