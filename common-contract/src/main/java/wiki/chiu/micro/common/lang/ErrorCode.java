package wiki.chiu.micro.common.lang;

public interface ErrorCode {

    int code();

    String defaultMessage();

    ErrorCategory category();
}
