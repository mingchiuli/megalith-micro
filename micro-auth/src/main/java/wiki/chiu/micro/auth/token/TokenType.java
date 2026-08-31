package wiki.chiu.micro.auth.token;

public enum TokenType {
    ACCESS("access"),

    REFRESH("refresh"),

    WEBSOCKET("websocket");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
