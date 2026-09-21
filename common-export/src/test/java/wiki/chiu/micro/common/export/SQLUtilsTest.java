package wiki.chiu.micro.common.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

class SQLUtilsTest {

    private record Row(
        Long id,
        Integer count,
        Boolean flag,
        LocalDateTime moment,
        LocalDate day,
        Date legacy,
        String text) {
    }

    private static final SqlTable<Row> ROWS =
        SqlTable.of(
            "t_row",
            SqlColumn.of("id", Row::id),
            SqlColumn.of("count", Row::count),
            SqlColumn.of("flag", Row::flag),
            SqlColumn.of("moment", Row::moment),
            SqlColumn.of("day", Row::day),
            SqlColumn.of("legacy", Row::legacy),
            SqlColumn.of("text", Row::text));

    @Test
    void formatsValuesByRuntimeType() {
        var row =
            new Row(
                7L,
                3,
                true,
                LocalDateTime.of(2026, 9, 19, 10, 11, 12),
                LocalDate.of(2026, 9, 19),
                new Date(0L),
                "plain");

        assertEquals(
            "INSERT INTO t_row (id, count, flag, moment, day, legacy, text) VALUES (7, 3, 1,"
                + " '2026-09-19 10:11:12', '2026-09-19', '"
                + new java.sql.Timestamp(0L)
                + "', 'plain');",
            SQLUtils.insertSql(List.of(row), ROWS));
    }

    @Test
    void rendersNullsAndFalse() {
        var row = new Row(null, null, false, null, null, null, null);

        assertEquals(
            "INSERT INTO t_row (id, count, flag, moment, day, legacy, text) VALUES (NULL, NULL,"
                + " 0, NULL, NULL, NULL, NULL);",
            SQLUtils.insertSql(List.of(row), ROWS));
    }

    @Test
    void escapesSqlStrings() {
        var row = new Row(1L, 0, false, null, null, null, "a'b\\c\n\r\t\u0000");

        assertEquals(
            "INSERT INTO t_row (id, count, flag, moment, day, legacy, text) VALUES (1, 0, 0,"
                + " NULL, NULL, NULL, 'a''b\\\\c\\n\\r\\t\\0');",
            SQLUtils.insertSql(List.of(row), ROWS));
    }

    @Test
    void emptyInputOrEmptyColumnsProduceNoStatement() {
        assertEquals("", SQLUtils.insertSql(List.of(), ROWS));
        assertEquals("", SQLUtils.insertSql(List.of(), SqlTable.of("t_empty")));
    }

    @Test
    void composeJoinsStatementsWithNewline() {
        assertEquals("SELECT 1;\nSELECT 2;", SQLUtils.compose("SELECT 1;", "SELECT 2;"));
    }
}
