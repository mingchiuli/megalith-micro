package wiki.chiu.micro.user.req;

import java.util.List;
import java.util.Optional;

public record UserEntityReq(

        Optional<Long> id,

        String username,

        String nickname,

        String avatar,

        String password,

        String email,

        String phone,

        Integer status,

        List<String> roles) {

        public UserEntityReq(UserEntityReq req, String password) {
                this(req.id, req.username, req.nickname, req.avatar, password, req.email, req.phone, req.status, req.roles);
        }

        public UserEntityReq(UserEntityRegisterReq req, Long id, Integer status, List<String> roles) {
                this(Optional.ofNullable(id), req.username(), req.nickname(), req.avatar(), req.password(), req.email(), req.phone(), status, roles);
        }
}
