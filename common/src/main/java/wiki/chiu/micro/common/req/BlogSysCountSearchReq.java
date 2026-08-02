package wiki.chiu.micro.common.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import wiki.chiu.micro.common.validation.DateRangeRequest;

import java.time.LocalDateTime;
import java.util.List;

public record BlogSysCountSearchReq(

        @Size(max = 20)
        String keywords,

        @Min(0)
        @Max(3)
        Integer status,

        LocalDateTime createStart,

        LocalDateTime createEnd,

        @NotNull
        @PositiveOrZero
        Long userId,

        @NotNull
        List<@NotBlank String> roles
) implements DateRangeRequest {

    @JsonIgnore
    @AssertTrue(message = "createStart and createEnd must both be absent or form an increasing range")
    public boolean isDateRangeValid() {
        return hasValidDateRange();
    }

    public static BlogSysCountSearchReq.BLogSysCountSearchReqBuilder builder() {
        return new BlogSysCountSearchReq.BLogSysCountSearchReqBuilder();
    }

    public static class BLogSysCountSearchReqBuilder {

        private String keywords;

        private Integer status;

        private LocalDateTime createStart;

        private LocalDateTime createEnd;

        private Long userId;

        private List<String> roles;

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder createStart(LocalDateTime createStart) {
            this.createStart = createStart;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder createEnd(LocalDateTime createEnd) {
            this.createEnd = createEnd;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BlogSysCountSearchReq.BLogSysCountSearchReqBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public BlogSysCountSearchReq build() {
            return new BlogSysCountSearchReq(keywords, status, createStart, createEnd, userId, roles);
        }
    }
}
