package wiki.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record RoleEntityVo(
    Long id,

    String name,

    String code,

    String remark,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updated,

    Integer status
) {
    public static RoleEntityVoBuilder builder() {
        return new RoleEntityVoBuilder(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public record RoleEntityVoBuilder(
        Long id,
        String name,
        String code,
        String remark,
        LocalDateTime created,
        LocalDateTime updated,
        Integer status
    ) {
        public RoleEntityVoBuilder id(Long id) {
            return new RoleEntityVoBuilder(
                id,
                name,
                code,
                remark,
                created,
                updated,
                status
            );
        }

        public RoleEntityVoBuilder name(String name) {
            return new RoleEntityVoBuilder(
                id,
                name,
                code,
                remark,
                created,
                updated,
                status
            );
        }

        public RoleEntityVoBuilder code(String code) {
            return new RoleEntityVoBuilder(
                id,
                name,
                code,
                remark,
                created,
                updated,
                status
            );
        }

        public RoleEntityVoBuilder remark(String remark) {
            return new RoleEntityVoBuilder(
                id,
                name,
                code,
                remark,
                created,
                updated,
                status
            );
        }

        public RoleEntityVoBuilder created(LocalDateTime created) {
            return new RoleEntityVoBuilder(
                id,
                name,
                code,
                remark,
                created,
                updated,
                status
            );
        }

        public RoleEntityVoBuilder updated(LocalDateTime updated) {
            return new RoleEntityVoBuilder(
                id,
                name,
                code,
                remark,
                created,
                updated,
                status
            );
        }

        public RoleEntityVoBuilder status(Integer status) {
            return new RoleEntityVoBuilder(
                id,
                name,
                code,
                remark,
                created,
                updated,
                status
            );
        }

        public RoleEntityVo build() {
            return new RoleEntityVo(
                id,
                name,
                code,
                remark,
                created,
                updated,
                status
            );
        }
    }
}
