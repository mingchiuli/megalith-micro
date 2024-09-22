package org.chiu.micro.blog.vo;


public class SensitiveContentVo {

    private Integer startIndex;

    private Integer endIndex;

    private Integer type;

    SensitiveContentVo(Integer startIndex, Integer endIndex, Integer type) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
    }

    public static SensitiveContentVoBuilder builder() {
        return new SensitiveContentVoBuilder();
    }

    public Integer getStartIndex() {
        return this.startIndex;
    }

    public Integer getEndIndex() {
        return this.endIndex;
    }

    public Integer getType() {
        return this.type;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SensitiveContentVo)) return false;
        final SensitiveContentVo other = (SensitiveContentVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$startIndex = this.getStartIndex();
        final Object other$startIndex = other.getStartIndex();
        if (this$startIndex == null ? other$startIndex != null : !this$startIndex.equals(other$startIndex))
            return false;
        final Object this$endIndex = this.getEndIndex();
        final Object other$endIndex = other.getEndIndex();
        if (this$endIndex == null ? other$endIndex != null : !this$endIndex.equals(other$endIndex)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SensitiveContentVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $startIndex = this.getStartIndex();
        result = result * PRIME + ($startIndex == null ? 43 : $startIndex.hashCode());
        final Object $endIndex = this.getEndIndex();
        result = result * PRIME + ($endIndex == null ? 43 : $endIndex.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    public String toString() {
        return "SensitiveContentVo(startIndex=" + this.getStartIndex() + ", endIndex=" + this.getEndIndex() + ", type=" + this.getType() + ")";
    }

    public static class SensitiveContentVoBuilder {
        private Integer startIndex;
        private Integer endIndex;
        private Integer type;

        SensitiveContentVoBuilder() {
        }

        public SensitiveContentVoBuilder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public SensitiveContentVoBuilder endIndex(Integer endIndex) {
            this.endIndex = endIndex;
            return this;
        }

        public SensitiveContentVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public SensitiveContentVo build() {
            return new SensitiveContentVo(this.startIndex, this.endIndex, this.type);
        }

        public String toString() {
            return "SensitiveContentVo.SensitiveContentVoBuilder(startIndex=" + this.startIndex + ", endIndex=" + this.endIndex + ", type=" + this.type + ")";
        }
    }
}
