package wiki.chiu.micro.blog.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SensitiveContentReq(

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        @PositiveOrZero(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        Integer startIndex,

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        @PositiveOrZero(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        Integer endIndex,

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        @Min(value = 1, message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        @Max(value = 3, message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        Integer type) {

    public static SensitiveContentReqBuilder builder() {
        return new SensitiveContentReqBuilder();
    }

    public static class SensitiveContentReqBuilder {
        private Integer startIndex;
        private Integer endIndex;
        private Integer type;

        public SensitiveContentReqBuilder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public SensitiveContentReqBuilder endIndex(Integer endIndex) {
            this.endIndex = endIndex;
            return this;
        }

        public SensitiveContentReqBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public SensitiveContentReq build() {
            return new SensitiveContentReq(this.startIndex, this.endIndex, this.type);
        }
    }
}
