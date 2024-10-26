package wiki.chiu.micro.user.vo;

public record RoleAuthorityVo(

        Long authorityId,

        String code,

        Boolean check) {

    public static RoleAuthorityVoBuilder builder() {
        return new RoleAuthorityVoBuilder();
    }

    public static class RoleAuthorityVoBuilder {
        private Long authorityId;
        private String code;
        private Boolean check;

        public RoleAuthorityVoBuilder authorityId(Long authorityId) {
            this.authorityId = authorityId;
            return this;
        }

        public RoleAuthorityVoBuilder code(String code) {
            this.code = code;
            return this;
        }

        public RoleAuthorityVoBuilder check(Boolean check) {
            this.check = check;
            return this;
        }

        public RoleAuthorityVo build() {
            return new RoleAuthorityVo(authorityId, code, check);
        }
    }
}
