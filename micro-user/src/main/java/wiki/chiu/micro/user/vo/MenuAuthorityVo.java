package wiki.chiu.micro.user.vo;

public record MenuAuthorityVo(

        Long authorityId,

        String code,

        Boolean check) {

    public static MenuAuthorityVoBuilder builder() {
        return new MenuAuthorityVoBuilder();
    }

    public static class MenuAuthorityVoBuilder {
        private Long authorityId;
        private String code;
        private Boolean check;

        public MenuAuthorityVoBuilder authorityId(Long authorityId) {
            this.authorityId = authorityId;
            return this;
        }

        public MenuAuthorityVoBuilder code(String code) {
            this.code = code;
            return this;
        }

        public MenuAuthorityVoBuilder check(Boolean check) {
            this.check = check;
            return this;
        }

        public MenuAuthorityVo build() {
            return new MenuAuthorityVo(authorityId, code, check);
        }
    }
}
