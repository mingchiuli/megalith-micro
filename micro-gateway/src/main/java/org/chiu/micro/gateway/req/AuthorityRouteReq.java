package org.chiu.micro.gateway.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorityRouteReq {
    
    private String method;

    private String routeMapping;

    private String token;
    
    private String ipAddr;
}
