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
        return new AuthorityVoBuilder(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public record AuthorityVoBuilder(
            Long id,
            String code,
            String remark,
            String prototype,
            String methodType,
            String routePattern,
            String serviceHost,
            Integer servicePort,
            LocalDateTime created,
            LocalDateTime updated,
            Integer type,
            Integer status) {

        public AuthorityVoBuilder id(Long id) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder code(String code) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder remark(String remark) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder prototype(String prototype) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder methodType(String methodType) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder routePattern(String routePattern) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder serviceHost(String serviceHost) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder servicePort(Integer servicePort) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder created(LocalDateTime created) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder updated(LocalDateTime updated) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder type(Integer type) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVoBuilder status(Integer status) {
            return new AuthorityVoBuilder(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }

        public AuthorityVo build() {
            return new AuthorityVo(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, created, updated, type, status);
        }
    }
}
