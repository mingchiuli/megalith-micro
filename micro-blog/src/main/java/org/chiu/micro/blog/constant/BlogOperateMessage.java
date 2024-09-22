package org.chiu.micro.blog.constant;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2021-12-13 10:46 AM
 */
public record BlogOperateMessage(

        Long blogId,

        BlogOperateEnum typeEnum,

        Integer year) implements Serializable {
}
