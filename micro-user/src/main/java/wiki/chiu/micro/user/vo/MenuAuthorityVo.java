package wiki.chiu.micro.user.vo;

public record MenuAuthorityVo(

        Long authorityId,

        String code,

        Boolean check) {

    public static MenuAuthorityVoBuilder builder() {
        return new MenuAuthorityVoBuilder(null, null, null);
    }

    public record MenuAuthorityVoBuilder(
            Long authorityId,
            String code,
            Boolean check) {

        public MenuAuthorityVoBuilder authorityId(Long authorityId) {
            return new MenuAuthorityVoBuilder(authorityId, code, check);
        }

        public MenuAuthorityVoBuilder code(String code) {
            return new MenuAuthorityVoBuilder(authorityId, code, check);
        }

        public MenuAuthorityVoBuilder check(Boolean check) {
            return new MenuAuthorityVoBuilder(authorityId, code, check);
        }

        public MenuAuthorityVo build() {
            return new MenuAuthorityVo(authorityId, code, check);
        }
    }
}
