package org.chiu.micro.exhibit.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author mingchiuli
 * @create 2023-04-19 1:50 am
 */
@Data
@Builder
public class VisitStatisticsVo {

    private Long dayVisit;

    private Long weekVisit;

    private Long monthVisit;

    private Long yearVisit;
}
