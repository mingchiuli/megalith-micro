package org.chiu.micro.websocket.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BlogEditPushActionReq {

    private Long id;

    //内容变动的部分
    private String contentChange;

    @NotNull
    private Integer operateTypeCode;

    @NotNull
    private Integer version;

    private Integer indexStart;

    private Integer indexEnd;

    @NotBlank
    private String field;

    private Integer paraNo;

    public BlogEditPushActionReq() {
    }

    public Long getId() {
        return this.id;
    }

    public String getContentChange() {
        return this.contentChange;
    }

    public @NotNull Integer getOperateTypeCode() {
        return this.operateTypeCode;
    }

    public @NotNull Integer getVersion() {
        return this.version;
    }

    public Integer getIndexStart() {
        return this.indexStart;
    }

    public Integer getIndexEnd() {
        return this.indexEnd;
    }

    public @NotBlank String getField() {
        return this.field;
    }

    public Integer getParaNo() {
        return this.paraNo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContentChange(String contentChange) {
        this.contentChange = contentChange;
    }

    public void setOperateTypeCode(@NotNull Integer operateTypeCode) {
        this.operateTypeCode = operateTypeCode;
    }

    public void setVersion(@NotNull Integer version) {
        this.version = version;
    }

    public void setIndexStart(Integer indexStart) {
        this.indexStart = indexStart;
    }

    public void setIndexEnd(Integer indexEnd) {
        this.indexEnd = indexEnd;
    }

    public void setField(@NotBlank String field) {
        this.field = field;
    }

    public void setParaNo(Integer paraNo) {
        this.paraNo = paraNo;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BlogEditPushActionReq)) return false;
        final BlogEditPushActionReq other = (BlogEditPushActionReq) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$contentChange = this.getContentChange();
        final Object other$contentChange = other.getContentChange();
        if (this$contentChange == null ? other$contentChange != null : !this$contentChange.equals(other$contentChange))
            return false;
        final Object this$operateTypeCode = this.getOperateTypeCode();
        final Object other$operateTypeCode = other.getOperateTypeCode();
        if (this$operateTypeCode == null ? other$operateTypeCode != null : !this$operateTypeCode.equals(other$operateTypeCode))
            return false;
        final Object this$version = this.getVersion();
        final Object other$version = other.getVersion();
        if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
        final Object this$indexStart = this.getIndexStart();
        final Object other$indexStart = other.getIndexStart();
        if (this$indexStart == null ? other$indexStart != null : !this$indexStart.equals(other$indexStart))
            return false;
        final Object this$indexEnd = this.getIndexEnd();
        final Object other$indexEnd = other.getIndexEnd();
        if (this$indexEnd == null ? other$indexEnd != null : !this$indexEnd.equals(other$indexEnd)) return false;
        final Object this$field = this.getField();
        final Object other$field = other.getField();
        if (this$field == null ? other$field != null : !this$field.equals(other$field)) return false;
        final Object this$paraNo = this.getParaNo();
        final Object other$paraNo = other.getParaNo();
        if (this$paraNo == null ? other$paraNo != null : !this$paraNo.equals(other$paraNo)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BlogEditPushActionReq;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $contentChange = this.getContentChange();
        result = result * PRIME + ($contentChange == null ? 43 : $contentChange.hashCode());
        final Object $operateTypeCode = this.getOperateTypeCode();
        result = result * PRIME + ($operateTypeCode == null ? 43 : $operateTypeCode.hashCode());
        final Object $version = this.getVersion();
        result = result * PRIME + ($version == null ? 43 : $version.hashCode());
        final Object $indexStart = this.getIndexStart();
        result = result * PRIME + ($indexStart == null ? 43 : $indexStart.hashCode());
        final Object $indexEnd = this.getIndexEnd();
        result = result * PRIME + ($indexEnd == null ? 43 : $indexEnd.hashCode());
        final Object $field = this.getField();
        result = result * PRIME + ($field == null ? 43 : $field.hashCode());
        final Object $paraNo = this.getParaNo();
        result = result * PRIME + ($paraNo == null ? 43 : $paraNo.hashCode());
        return result;
    }

    public String toString() {
        return "BlogEditPushActionReq(id=" + this.getId() + ", contentChange=" + this.getContentChange() + ", operateTypeCode=" + this.getOperateTypeCode() + ", version=" + this.getVersion() + ", indexStart=" + this.getIndexStart() + ", indexEnd=" + this.getIndexEnd() + ", field=" + this.getField() + ", paraNo=" + this.getParaNo() + ")";
    }
}
