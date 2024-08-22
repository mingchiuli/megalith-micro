package org.chiu.micro.blog.req;

import lombok.Data;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;


/**
 * DeleteBlogsReq
 */
@Data
public class DeleteBlogsReq {

    @NotEmpty
    private List<Long> ids;
}