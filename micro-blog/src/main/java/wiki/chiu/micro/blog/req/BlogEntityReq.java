package wiki.chiu.micro.blog.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

import static wiki.chiu.micro.common.lang.Const.URL_REGEX;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:26 pm
 */
public record BlogEntityReq(

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        Optional<@Positive(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}") Long> id,

        @NotBlank(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        String title,

        @NotBlank(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        String description,

        @NotBlank(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        String content,

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        @Min(value = 0, message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        @Max(value = 3, message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        Integer status,

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        @Pattern(regexp = "^$|" + URL_REGEX, message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        String link,

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        List<@NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}") @Valid SensitiveContentReq> sensitiveContentList) {

    @JsonIgnore
    @AssertTrue(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
    public boolean isSensitiveContentRangeValid() {
        if (content == null || sensitiveContentList == null) {
            return true;
        }
        int contentLength = content.length();
        return sensitiveContentList.stream().allMatch(item -> isValidRange(item, contentLength));
    }

    private static boolean isValidRange(SensitiveContentReq item, int contentLength) {
        if (item == null || item.startIndex() == null || item.endIndex() == null) {
            return true;
        }
        return item.startIndex() >= 0
                && item.startIndex() < item.endIndex()
                && item.endIndex() <= contentLength;
    }
}
