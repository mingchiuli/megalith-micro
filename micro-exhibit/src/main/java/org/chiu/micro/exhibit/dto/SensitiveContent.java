package org.chiu.micro.exhibit.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensitiveContent implements Serializable {

    private Integer startIndex;

    private Integer endIndex;

    private Integer type;
}
