package org.chiu.micro.websocket.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class StompMessageDto implements Serializable {

    private Integer version;

    private Long userId;

    private Long blogId;

    private Integer type;
}
