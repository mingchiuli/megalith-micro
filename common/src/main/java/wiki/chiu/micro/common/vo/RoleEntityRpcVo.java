package wiki.chiu.micro.common.vo;

import java.time.LocalDateTime;


public record RoleEntityRpcVo(

        Long id,

        String name,

        String code,

        String remark,

        LocalDateTime created,

        LocalDateTime updated,

        Integer status) {

    public static RoleEntityRpcVo.RoleEntityRpcVoBuilder builder() {
        return new RoleEntityRpcVo.RoleEntityRpcVoBuilder();
    }

    public static class RoleEntityRpcVoBuilder {
        private Long id;
        private String name;
        private String code;
        private String remark;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;


        public RoleEntityRpcVo.RoleEntityRpcVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoleEntityRpcVo.RoleEntityRpcVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleEntityRpcVo.RoleEntityRpcVoBuilder code(String code) {
            this.code = code;
            return this;
        }

        public RoleEntityRpcVo.RoleEntityRpcVoBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public RoleEntityRpcVo.RoleEntityRpcVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public RoleEntityRpcVo.RoleEntityRpcVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public RoleEntityRpcVo.RoleEntityRpcVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public RoleEntityRpcVo build() {
            return new RoleEntityRpcVo(id, name, code, remark, created, updated, status);
        }

    }
}

