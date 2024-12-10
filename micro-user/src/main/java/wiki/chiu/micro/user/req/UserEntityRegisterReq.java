package wiki.chiu.micro.user.req;


public record UserEntityRegisterReq(

        Long id,

        String username,

        String nickname,

        String avatar,

        String password,

        String confirmPassword,

        String email,

        String phone,

        String token) {

        public UserEntityRegisterReq(UserEntityRegisterReq req, String phone) {
                this(req.id, req.username, req.nickname, req.avatar, req.password, req.confirmPassword, req.email, phone, req.token);
        }
}
