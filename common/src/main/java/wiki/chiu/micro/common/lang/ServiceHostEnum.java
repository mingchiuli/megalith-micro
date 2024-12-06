package wiki.chiu.micro.common.lang;

public enum ServiceHostEnum {

    MICRO_AUTH("micro-auth", "鉴权服务"),

    MICRO_BLOG("micro-blog", "博客服务"),

    MICRO_EXHIBIT("micro-exhibit", "展示服务"),

    MICRO_SEARCH("micro-search", "搜索服务"),

    MICRO_USER("micro-user", "用户服务"),

    MICRO_WEBSOCKET("micro-websocket", "长连接服务"),

    MICRO_GATEWAY("micro-gateway", "网关");

    private final String serviceHost;

    private final String description;

    ServiceHostEnum(String serviceHost, String description) {
        this.serviceHost = serviceHost;
        this.description = description;
    }

    public static ServiceHostEnum getInstance(String serviceHost) {
        for (ServiceHostEnum value : ServiceHostEnum.values()) {
            if (value.getServiceHost().equals(serviceHost)) {
                return value;
            }
        }
        throw new IllegalArgumentException();
    }

    public String getServiceHost() {
        return this.serviceHost;
    }

    public String getDescription() {
        return this.description;
    }
}
