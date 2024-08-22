package org.chiu.micro.gateway.req;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class SensitiveContentReq {

    private Integer startIndex;

    private Integer endIndex;

    private Integer type;
}
