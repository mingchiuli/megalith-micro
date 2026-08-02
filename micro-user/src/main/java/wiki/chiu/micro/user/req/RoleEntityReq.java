package wiki.chiu.micro.user.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Optional;

/**
 * @author mingchiuli
 * @create 2022-12-06 8:57 pm
 */
public record RoleEntityReq(

        @NotNull
        Optional<@Positive Long> id,

        @NotBlank
        String name,

        @NotBlank
        String code,

        @NotNull
        String remark,

        @NotNull(message = "{wiki.chiu.micro.user.valid.ListValue.message}")
        @Min(value = 0, message = "{wiki.chiu.micro.user.valid.ListValue.message}")
        @Max(value = 1, message = "{wiki.chiu.micro.user.valid.ListValue.message}")
        Integer status) {
}
