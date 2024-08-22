package org.chiu.micro.exhibit.constant;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mingchiuli
 * @create 2021-12-13 10:46 AM
 */
@Data
@AllArgsConstructor
public class BlogOperateMessage implements Serializable {

    private Long blogId;

    private BlogOperateEnum typeEnum;

    private Integer year;

}
