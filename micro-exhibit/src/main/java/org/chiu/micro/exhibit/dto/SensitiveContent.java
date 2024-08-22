package org.chiu.micro.exhibit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensitiveContent {

    private Integer startIndex;

    private Integer endIndex;

    private Integer type;
}
