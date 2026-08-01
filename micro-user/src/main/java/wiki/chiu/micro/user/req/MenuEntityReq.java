package wiki.chiu.micro.user.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

/**
 * @author mingchiuli
 * @create 2022-12-04 6:23 pm
 */
public record MenuEntityReq(

        Optional<Long> id,

        @NotNull(message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        Long parentId,

        @NotBlank(message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        String title,

        @NotBlank(message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        String name,

        String url,

        String component,

        String icon,

        @NotNull(message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        Integer orderNum,

        @NotNull(message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        @Min(value = 0, message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        @Max(value = 2, message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        Integer type,

        @NotNull(message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        @Min(value = 0, message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        @Max(value = 1, message = "{wiki.chiu.micro.user.valid.MenuValue.message}")
        Integer status) {
}
