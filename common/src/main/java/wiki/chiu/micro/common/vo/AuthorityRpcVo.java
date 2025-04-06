package wiki.chiu.micro.common.vo;

import java.io.Serializable;

public record AuthorityRpcVo(
        Long id,
        String code,
        String remark,
        String prototype,
        String methodType,
        String routePattern,
        String serviceHost,
        Integer servicePort,
        Integer type,
        Integer status) implements Serializable {

    public static AuthorityRpcVoBuilder builder() {
        return new AuthorityRpcVoBuilder(null, null, null, null, null, null, null, null, null, null);
    }

    public record AuthorityRpcVoBuilder(
            Long id,
            String code,
            String remark,
            String prototype,
            String methodType,
            String routePattern,
            String serviceHost,
            Integer servicePort,
            Integer type,
            Integer status
    ) {

        public AuthorityRpcVoBuilder id(Long id) {
            return new AuthorityRpcVoBuilder(id, this.code, this.remark, this.prototype, this.methodType, this.routePattern, this.serviceHost, this.servicePort, this.type, this.status);
        }

        public AuthorityRpcVoBuilder code(String code) {
            return new AuthorityRpcVoBuilder(this.id, code, this.remark, this.prototype, this.methodType, this.routePattern, this.serviceHost, this.servicePort, this.type, this.status);
        }

        public AuthorityRpcVoBuilder remark(String remark) {
            return new AuthorityRpcVoBuilder(this.id, this.code, remark, this.prototype, this.methodType, this.routePattern, this.serviceHost, this.servicePort, this.type, this.status);
        }

        public AuthorityRpcVoBuilder prototype(String prototype) {
            return new AuthorityRpcVoBuilder(this.id, this.code, this.remark, prototype, this.methodType, this.routePattern, this.serviceHost, this.servicePort, this.type, this.status);
        }

        public AuthorityRpcVoBuilder methodType(String methodType) {
            return new AuthorityRpcVoBuilder(this.id, this.code, this.remark, this.prototype, methodType, this.routePattern, this.serviceHost, this.servicePort, this.type, this.status);
        }

        public AuthorityRpcVoBuilder routePattern(String routePattern) {
            return new AuthorityRpcVoBuilder(this.id, this.code, this.remark, this.prototype, this.methodType, routePattern, this.serviceHost, this.servicePort, this.type, this.status);
        }

        public AuthorityRpcVoBuilder serviceHost(String serviceHost) {
            return new AuthorityRpcVoBuilder(this.id, this.code, this.remark, this.prototype, this.methodType, this.routePattern, serviceHost, this.servicePort, this.type, this.status);
        }

        public AuthorityRpcVoBuilder servicePort(Integer servicePort) {
            return new AuthorityRpcVoBuilder(this.id, this.code, this.remark, this.prototype, this.methodType, this.routePattern, this.serviceHost, servicePort, this.type, this.status);
        }

        public AuthorityRpcVoBuilder type(Integer type) {
            return new AuthorityRpcVoBuilder(this.id, this.code, this.remark, this.prototype, this.methodType, this.routePattern, this.serviceHost, this.servicePort, type, this.status);
        }

        public AuthorityRpcVoBuilder status(Integer status) {
            return new AuthorityRpcVoBuilder(this.id, this.code, this.remark, this.prototype, this.methodType, this.routePattern, this.serviceHost, this.servicePort, this.type, status);
        }

        public AuthorityRpcVo build() {
            return new AuthorityRpcVo(id, code, remark, prototype, methodType, routePattern, serviceHost, servicePort, type, status);
        }
    }
}