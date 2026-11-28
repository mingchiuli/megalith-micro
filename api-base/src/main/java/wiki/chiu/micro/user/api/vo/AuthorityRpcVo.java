package wiki.chiu.micro.user.api.vo;

public record AuthorityRpcVo(
    Long id,
    String code,
    String remark,
    String protocol,
    String methodType,
    String routePattern,
    String serviceHost,
    Integer servicePort,
    Integer type,
    Integer status) {

    public static AuthorityRpcVoBuilder builder() {
        return new AuthorityRpcVoBuilder();
    }

    public static class AuthorityRpcVoBuilder {
        private Long id;
        private String code;
        private String remark;
        private String protocol;
        private String methodType;
        private String routePattern;
        private String serviceHost;
        private Integer servicePort;
        private Integer type;
        private Integer status;

        public AuthorityRpcVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuthorityRpcVoBuilder code(String code) {
            this.code = code;
            return this;
        }

        public AuthorityRpcVoBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public AuthorityRpcVoBuilder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public AuthorityRpcVoBuilder methodType(String methodType) {
            this.methodType = methodType;
            return this;
        }

        public AuthorityRpcVoBuilder routePattern(String routePattern) {
            this.routePattern = routePattern;
            return this;
        }

        public AuthorityRpcVoBuilder serviceHost(String serviceHost) {
            this.serviceHost = serviceHost;
            return this;
        }

        public AuthorityRpcVoBuilder servicePort(Integer servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public AuthorityRpcVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public AuthorityRpcVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public AuthorityRpcVo build() {
            return new AuthorityRpcVo(
                id,
                code,
                remark,
                protocol,
                methodType,
                routePattern,
                serviceHost,
                servicePort,
                type,
                status);
        }
    }
}
