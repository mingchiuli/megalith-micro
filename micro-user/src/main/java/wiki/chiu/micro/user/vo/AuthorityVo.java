package wiki.chiu.micro.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AuthorityVo(

        Long id,

        String code,

        String remark,

        String prototype,

        String methodType,

        String routePattern,

        String serviceHost,

        Integer servicePort,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updated,

        Integer type,

        Integer status) {


    public static AuthorityVoBuilder builder() {
        return new AuthorityVoBuilder();
    }

    public static class AuthorityVoBuilder {
        private Long id;
        private String code;
        private String remark;
        private String prototype;
        private String methodType;
        private String routePattern;
        private String serviceHost;
        private Integer servicePort;
        private LocalDateTime created;
        private LocalDateTime updated;
        private Integer type;
        private Integer status;

        public AuthorityVoBuilder id(Long id) {
            this.id = id;
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

        public AuthorityVoBuilder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public AuthorityVoBuilder updated(LocalDateTime updated) {
            this.updated = updated;
            return this;
        }

        public AuthorityVoBuilder type(Integer type) {
            this.type = type;
            return this;
        }

        public AuthorityVoBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public AuthorityVo build() {
            return new AuthorityVo(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }
    }
}
