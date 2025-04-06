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

    public static RoleEntityRpcVo builder() {
        return new RoleEntityRpcVo(null, null, null, null, null, null, null);
    }

    public record RoleEntityRpcVoBuilder(
            Long id,
            String name,
            String code,
            String remark,
            LocalDateTime created,
            LocalDateTime updated,
            Integer status
    ) {

        public RoleEntityRpcVoBuilder id(Long id) {
            return new RoleEntityRpcVoBuilder(id, this.name, this.code, this.remark, this.created, this.updated, this.status);
        }

        public RoleEntityRpcVoBuilder name(String name) {
            return new RoleEntityRpcVoBuilder(this.id, name, this.code, this.remark, this.created, this.updated, this.status);
        }

        public RoleEntityRpcVoBuilder code(String code) {
            return new RoleEntityRpcVoBuilder(this.id, this.name, code, this.remark, this.created, this.updated, this.status);
        }

        public RoleEntityRpcVoBuilder remark(String remark) {
            return new RoleEntityRpcVoBuilder(this.id, this.name, this.code, remark, this.created, this.updated, this.status);
        }

        public RoleEntityRpcVoBuilder created(LocalDateTime created) {
            return new RoleEntityRpcVoBuilder(this.id, this.name, this.code, this.remark, created, this.updated, this.status);
        }

        public RoleEntityRpcVoBuilder updated(LocalDateTime updated) {
            return new RoleEntityRpcVoBuilder(this.id, this.name, this.code, this.remark, this.created, updated, this.status);
        }

        public RoleEntityRpcVoBuilder status(Integer status) {
            return new RoleEntityRpcVoBuilder(this.id, this.name, this.code, this.remark, this.created, this.updated, status);
        }

        public RoleEntityRpcVo build() {
            return new RoleEntityRpcVo(id, name, code, remark, created, updated, status);
        }
    }
}