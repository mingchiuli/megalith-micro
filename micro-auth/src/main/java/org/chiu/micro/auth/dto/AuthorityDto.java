package org.chiu.micro.auth.dto;


public class AuthorityDto {

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

    public AuthorityDto() {
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getRemark() {
        return this.remark;
    }

    public String getPrototype() {
        return this.prototype;
    }

    public String getMethodType() {
        return this.methodType;
    }

    public String getRoutePattern() {
        return this.routePattern;
    }

    public String getServiceHost() {
        return this.serviceHost;
    }

    public Integer getServicePort() {
        return this.servicePort;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setPrototype(String prototype) {
        this.prototype = prototype;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public void setRoutePattern(String routePattern) {
        this.routePattern = routePattern;
    }

    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    public void setServicePort(Integer servicePort) {
        this.servicePort = servicePort;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthorityDto)) return false;
        final AuthorityDto other = (AuthorityDto) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$code = this.getCode();
        final Object other$code = other.getCode();
        if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
        final Object this$remark = this.getRemark();
        final Object other$remark = other.getRemark();
        if (this$remark == null ? other$remark != null : !this$remark.equals(other$remark)) return false;
        final Object this$prototype = this.getPrototype();
        final Object other$prototype = other.getPrototype();
        if (this$prototype == null ? other$prototype != null : !this$prototype.equals(other$prototype)) return false;
        final Object this$methodType = this.getMethodType();
        final Object other$methodType = other.getMethodType();
        if (this$methodType == null ? other$methodType != null : !this$methodType.equals(other$methodType))
            return false;
        final Object this$routePattern = this.getRoutePattern();
        final Object other$routePattern = other.getRoutePattern();
        if (this$routePattern == null ? other$routePattern != null : !this$routePattern.equals(other$routePattern))
            return false;
        final Object this$serviceHost = this.getServiceHost();
        final Object other$serviceHost = other.getServiceHost();
        if (this$serviceHost == null ? other$serviceHost != null : !this$serviceHost.equals(other$serviceHost))
            return false;
        final Object this$servicePort = this.getServicePort();
        final Object other$servicePort = other.getServicePort();
        if (this$servicePort == null ? other$servicePort != null : !this$servicePort.equals(other$servicePort))
            return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AuthorityDto;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $code = this.getCode();
        result = result * PRIME + ($code == null ? 43 : $code.hashCode());
        final Object $remark = this.getRemark();
        result = result * PRIME + ($remark == null ? 43 : $remark.hashCode());
        final Object $prototype = this.getPrototype();
        result = result * PRIME + ($prototype == null ? 43 : $prototype.hashCode());
        final Object $methodType = this.getMethodType();
        result = result * PRIME + ($methodType == null ? 43 : $methodType.hashCode());
        final Object $routePattern = this.getRoutePattern();
        result = result * PRIME + ($routePattern == null ? 43 : $routePattern.hashCode());
        final Object $serviceHost = this.getServiceHost();
        result = result * PRIME + ($serviceHost == null ? 43 : $serviceHost.hashCode());
        final Object $servicePort = this.getServicePort();
        result = result * PRIME + ($servicePort == null ? 43 : $servicePort.hashCode());
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        return result;
    }

    public String toString() {
        return "AuthorityDto(id=" + this.getId() + ", name=" + this.getName() + ", code=" + this.getCode() + ", remark=" + this.getRemark() + ", prototype=" + this.getPrototype() + ", methodType=" + this.getMethodType() + ", routePattern=" + this.getRoutePattern() + ", serviceHost=" + this.getServiceHost() + ", servicePort=" + this.getServicePort() + ", status=" + this.getStatus() + ")";
    }
}
