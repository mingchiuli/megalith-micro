package wiki.chiu.micro.blog.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Optional;

import static wiki.chiu.micro.common.lang.Const.URL_REGEX;


/**
 * @author mingchiuli
 * @create 2022-12-01 9:26 pm
 */
public record BlogEntityReq(

        @NotNull(message = "{wiki.chiu.micro.blog.valid.BlogSave.message}")
        Optional<Long> id,

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
}
