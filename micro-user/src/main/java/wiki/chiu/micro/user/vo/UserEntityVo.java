package wiki.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record UserEntityVo(
    Long id,

    String username,

    String nickname,

    String avatar,

    String email,

    String phone,

    Integer status,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updated,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastLogin,

    List<String> roles
) {
    public static UserEntityVoBuilder builder() {
        return new UserEntityVoBuilder(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
                Collections.emptyList()
        );
    }

    public record UserEntityVoBuilder(
        Long id,
        String username,
        String nickname,
        String avatar,
        String email,
        String phone,
        Integer status,
        LocalDateTime created,
        LocalDateTime updated,
        LocalDateTime lastLogin,
        List<String> roles
    ) {
        public UserEntityVoBuilder id(Long id) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder username(String username) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder nickname(String nickname) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder avatar(String avatar) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder email(String email) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder phone(String phone) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder status(Integer status) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder created(LocalDateTime created) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder updated(LocalDateTime updated) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder lastLogin(LocalDateTime lastLogin) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVoBuilder roles(List<String> roles) {
            return new UserEntityVoBuilder(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }

        public UserEntityVo build() {
            return new UserEntityVo(
                id,
                username,
                nickname,
                avatar,
                email,
                phone,
                status,
                created,
                updated,
                lastLogin,
                roles
            );
        }
    }
}
