package org.chiu.micro.gateway.req;

import lombok.Data;
import java.util.List;


/**
 * DeleteBlogsReq
 */
@Data
public class DeleteBlogsReq {

    private List<Long> ids;
}