package wiki.chiu.micro.common.exception;


import wiki.chiu.micro.common.lang.ExceptionMessage;

public class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message);
    }

    public BaseException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMsg());
    }
}
