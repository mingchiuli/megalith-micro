package org.chiu.micro.user.vo;

public class RoleAuthorityVo {

    private Long authorityId;

    private String code;

    private Boolean check;

    RoleAuthorityVo(Long authorityId, String code, Boolean check) {
        this.authorityId = authorityId;
        this.code = code;
        this.check = check;
    }

    public static RoleAuthorityVoBuilder builder() {
        return new RoleAuthorityVoBuilder();
    }

    public Long getAuthorityId() {
        return this.authorityId;
    }

    public String getCode() {
        return this.code;
    }

    public Boolean getCheck() {
        return this.check;
    }

    public void setAuthorityId(Long authorityId) {
        this.authorityId = authorityId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RoleAuthorityVo)) return false;
        final RoleAuthorityVo other = (RoleAuthorityVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$authorityId = this.getAuthorityId();
        final Object other$authorityId = other.getAuthorityId();
        if (this$authorityId == null ? other$authorityId != null : !this$authorityId.equals(other$authorityId))
            return false;
        final Object this$code = this.getCode();
        final Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        final Object this$check = this.getCheck();
        final Object other$check = other.getCheck();
        if (this$check == null ? other$check != null : !this$check.equals(other$check)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RoleAuthorityVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $authorityId = this.getAuthorityId();
        result = result * PRIME + ($authorityId == null ? 43 : $authorityId.hashCode());
        final Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        final Object $check = this.getCheck();
        result = result * PRIME + ($check == null ? 43 : $check.hashCode());
        return result;
    }

    public String toString() {
        return "RoleAuthorityVo(authorityId=" + this.getAuthorityId() + ", code=" + this.getCode() + ", check=" + this.getCheck() + ")";
    }

    public static class RoleAuthorityVoBuilder {
        private Long authorityId;
        private String code;
        private Boolean check;

        RoleAuthorityVoBuilder() {
        }

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
            return new RoleAuthorityVo(this.authorityId, this.code, this.check);
        }

        public String toString() {
            return "RoleAuthorityVo.RoleAuthorityVoBuilder(authorityId=" + this.authorityId + ", code=" + this.code + ", check=" + this.check + ")";
        }
    }
}
