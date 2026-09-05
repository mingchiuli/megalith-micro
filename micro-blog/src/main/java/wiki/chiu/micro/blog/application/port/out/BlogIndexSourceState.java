package wiki.chiu.micro.blog.application.port.out;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;

public interface BlogIndexSourceState {

    BlogIndexSourceStatus status();
}
