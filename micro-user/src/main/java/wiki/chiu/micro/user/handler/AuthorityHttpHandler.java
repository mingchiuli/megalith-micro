package wiki.chiu.micro.user.handler;

import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.user.req.AuthorityEntityReq;
import wiki.chiu.micro.user.service.AuthorityService;
import wiki.chiu.micro.user.vo.AuthorityVo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorityHttpHandler {

    private final AuthorityService authorityService;

    public AuthorityHttpHandler(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public Result<List<AuthorityVo>> list() {
        return Result.success(authorityService::findAll);
    }

    public Result<AuthorityVo> info(Long id) {
        return Result.success(() -> authorityService.findById(id));
    }

    public Result<Void> saveOrUpdate(AuthorityEntityReq req) {
        return Result.success(() -> authorityService.saveOrUpdate(req));
    }

    public Result<Void> delete(List<Long> ids) {
        return Result.success(() -> authorityService.deleteAuthorities(ids));
    }

    public byte[] download() {
        return authorityService.download();
    }
    
}
