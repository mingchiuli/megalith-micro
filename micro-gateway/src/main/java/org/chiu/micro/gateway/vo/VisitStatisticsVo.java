package org.chiu.micro.gateway.vo;

import lombok.Data;

/**
 * @author mingchiuli
 * @create 2023-04-19 1:50 am
 */
@Data
public class VisitStatisticsVo {

    private Long dayVisit;

    private Long weekVisit;

    private Long monthVisit;

    private Long yearVisit;
}
