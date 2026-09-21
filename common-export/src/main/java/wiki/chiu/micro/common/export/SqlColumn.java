package wiki.chiu.micro.common.export;

import java.util.function.Function;

/**
 * Describes one exported column: its SQL name and how to read the value from an entity.
 */
public record SqlColumn<E>(String name, Function<E, ?> value) {

    public static <E> SqlColumn<E> of(String name, Function<E, ?> value) {
        return new SqlColumn<>(name, value);
    }
}
