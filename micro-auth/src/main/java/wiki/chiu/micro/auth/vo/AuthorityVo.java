package wiki.chiu.micro.auth.vo;


public record AuthorityVo(
        Long id,

        String name,

        String code,

        String remark,

        String prototype,

        String methodType,

        String routePattern,

        String serviceHost,

        Integer servicePort,

        Integer status) {

    public static AuthorityVoBuilder builder() {
        return new AuthorityVoBuilder();
    }

    public static class AuthorityVoBuilder {
        private Long id;
        private String name;
        private String code;
        private String remark;
        private String prototype;
        private String methodType;
        private String routePattern;
        private String serviceHost;
        private Integer servicePort;
        private Integer status;

        public AuthorityVoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuthorityVoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AuthorityVoBuilder code(String code) {
            this.code = code;
            return this;
        }

        public AuthorityVoBuilder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public AuthorityVoBuilder prototype(String prototype) {
            this.prototype = prototype;
            return this;
        }

        public AuthorityVoBuilder methodType(String methodType) {
            this.methodType = methodType;
            return this;
        }

        public AuthorityVoBuilder routePattern(String routePattern) {
            this.routePattern = routePattern;
            return this;
        }

        public AuthorityVoBuilder serviceHost(String serviceHost) {
            this.serviceHost = serviceHost;
            return this;
        }

        public AuthorityVoBuilder servicePort(Integer servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public AuthorityVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public AuthorityVo build() {
            return new AuthorityVo(id, name, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, status);
        }
    }
}
