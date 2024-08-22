package org.chiu.micro.exhibit.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author limingjiu
 * @Date 2024/5/10 11:15
 **/
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class BlogDescriptionDto implements Serializable {

    private Long id;

    private String title;

    private String description;

    private Integer status;

    private LocalDateTime created;

    private String link;
}
