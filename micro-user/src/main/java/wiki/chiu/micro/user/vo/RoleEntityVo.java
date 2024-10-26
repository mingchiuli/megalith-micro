package wiki.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record RoleEntityVo(

        Long id,

        String name,

        String code,

        String remark,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updated,

        Integer status) {

    public static RoleEntityVoBuilder builder() {
        return new RoleEntityVoBuilder();
    }

    public static class RoleEntityVoBuilder {
        private Long id;
        private String name;
        private String code;
        private String remark;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;


        public RoleEntityVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoleEntityVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleEntityVoBuilder code(String code) {
            this.code = code;
            return this;
        }

        public RoleEntityVoBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public RoleEntityVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public RoleEntityVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public RoleEntityVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public RoleEntityVo build() {
            return new RoleEntityVo(id, name, code, remark, created, updated, status);
        }
    }
}
