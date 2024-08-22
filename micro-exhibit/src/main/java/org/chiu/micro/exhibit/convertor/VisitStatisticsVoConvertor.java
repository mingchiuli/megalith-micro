package org.chiu.micro.exhibit.convertor;

import org.chiu.micro.exhibit.vo.VisitStatisticsVo;

import java.util.List;

public class VisitStatisticsVoConvertor {

    private VisitStatisticsVoConvertor() {}

    public static VisitStatisticsVo convert(List<Long> items) {
        return VisitStatisticsVo.builder()
                .dayVisit(items.get(0))
                .weekVisit(items.get(1))
                .monthVisit(items.get(2))
                .yearVisit(items.get(3))
                .build();
    }
}
