package wiki.chiu.micro.user.api.vo;

import java.util.List;

public record UserAuthContextRpcVo(Long userId, Integer status, List<String> roles) {}
