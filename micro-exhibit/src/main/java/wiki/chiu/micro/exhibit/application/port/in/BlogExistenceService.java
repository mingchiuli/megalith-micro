package wiki.chiu.micro.exhibit.application.port.in;

public interface BlogExistenceService {

    void check(Long blogId);

    void markPresent(Long blogId);

    void markAbsent(Long blogId);

    void rebuildIfRequired();
}
