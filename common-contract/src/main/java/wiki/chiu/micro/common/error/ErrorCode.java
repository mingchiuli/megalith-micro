package wiki.chiu.micro.common.error;

public interface ErrorCode {

    int code();

    String defaultMessage();

    ErrorCategory category();
}
