package wiki.chiu.micro.blog.application.port.in;

public interface BlogStatisticsSync {

    long synchronize(int batchSize);
}
