package org.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class RoleEntityVo {

    private Long id;

    private String name;

    private String code;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    private Integer status;

    RoleEntityVo(Long id, String name, String code, String remark, LocalDateTime created, LocalDateTime updated, Integer status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.remark = remark;
        this.created = created;
        this.updated = updated;
        this.status = status;
    }

    public static RoleEntityVoBuilder builder() {
        return new RoleEntityVoBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getRemark() {
        return this.remark;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getUpdated() {
        return this.updated;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RoleEntityVo)) return false;
        final RoleEntityVo other = (RoleEntityVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$code = this.getCode();
        final Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        final Object this$remark = this.getRemark();
        final Object other$remark = other.getRemark();
        if (this$remark == null ? other$remark != null : !this$remark.equals(other$remark)) return false;
        final Object this$created = this.getCreated();
        final Object other$created = other.getCreated();
        if (this$created == null ? other$created != null : !this$created.equals(other$created)) return false;
        final Object this$updated = this.getUpdated();
        final Object other$updated = other.getUpdated();
        if (this$updated == null ? other$updated != null : !this$updated.equals(other$updated)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RoleEntityVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        final Object $remark = this.getRemark();
        result = result * PRIME + ($remark == null ? 43 : $remark.hashCode());
        final Object $created = this.getCreated();
        result = result * PRIME + ($created == null ? 43 : $created.hashCode());
        final Object $updated = this.getUpdated();
        result = result * PRIME + ($updated == null ? 43 : $updated.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        return result;
    }

    public String toString() {
        return "RoleEntityVo(id=" + this.getId() + ", name=" + this.getName() + ", code=" + this.getCode() + ", remark=" + this.getRemark() + ", created=" + this.getCreated() + ", updated=" + this.getUpdated() + ", status=" + this.getStatus() + ")";
    }

    public static class RoleEntityVoBuilder {
        private Long id;
        private String name;
        private String code;
        private String remark;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer status;

        RoleEntityVoBuilder() {
        }

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

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public RoleEntityVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        public RoleEntityVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public RoleEntityVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public RoleEntityVo build() {
            return new RoleEntityVo(this.id, this.name, this.code, this.remark, this.created, this.updated, this.status);
        }

        public String toString() {
            return "RoleEntityVo.RoleEntityVoBuilder(id=" + this.id + ", name=" + this.name + ", code=" + this.code + ", remark=" + this.remark + ", created=" + this.created + ", updated=" + this.updated + ", status=" + this.status + ")";
        }
    }
}
