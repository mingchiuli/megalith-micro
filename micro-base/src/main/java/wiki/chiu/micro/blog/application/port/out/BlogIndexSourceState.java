package wiki.chiu.micro.blog.application.port.out;

import wiki.chiu.micro.blog.application.model.IndexSourceStatus;

public interface BlogIndexSourceState {

    boolean ready();

    IndexSourceStatus status();
}
