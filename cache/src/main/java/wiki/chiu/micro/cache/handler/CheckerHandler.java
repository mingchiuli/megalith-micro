package wiki.chiu.micro.cache.handler;

public abstract class CheckerHandler {

    public boolean supports(Class<? extends CheckerHandler> clazz) {
        return clazz.equals(this.getClass());
    }

    public abstract void handle(Object[] args);
}
