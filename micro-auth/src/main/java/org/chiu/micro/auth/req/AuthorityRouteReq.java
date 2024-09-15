package org.chiu.micro.auth.req;

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
