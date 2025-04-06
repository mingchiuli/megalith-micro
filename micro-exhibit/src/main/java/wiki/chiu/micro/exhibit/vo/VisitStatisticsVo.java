package wiki.chiu.micro.exhibit.vo;

/**
 * @author mingchiuli
 * @create 2023-04-19 1:50 am
 */
public record VisitStatisticsVo(
        Long dayVisit,

        Long weekVisit,

        Long monthVisit,

        Long yearVisit
) {
    public static VisitStatisticsVoBuilder builder() {
        return new VisitStatisticsVoBuilder(null, null, null, null);
    }

    public record VisitStatisticsVoBuilder(
            Long dayVisit,
            Long weekVisit,
            Long monthVisit,
            Long yearVisit
    ) {
        public VisitStatisticsVoBuilder dayVisit(Long dayVisit) {
            return new VisitStatisticsVoBuilder(dayVisit, weekVisit, monthVisit, yearVisit);
        }

        public VisitStatisticsVoBuilder weekVisit(Long weekVisit) {
            return new VisitStatisticsVoBuilder(dayVisit, weekVisit, monthVisit, yearVisit);
        }

        public VisitStatisticsVoBuilder monthVisit(Long monthVisit) {
            return new VisitStatisticsVoBuilder(dayVisit, weekVisit, monthVisit, yearVisit);
        }

        public VisitStatisticsVoBuilder yearVisit(Long yearVisit) {
            return new VisitStatisticsVoBuilder(dayVisit, weekVisit, monthVisit, yearVisit);
        }

        public VisitStatisticsVo build() {
            return new VisitStatisticsVo(
                dayVisit,
                weekVisit,
                monthVisit,
                yearVisit
            );
        }
    }
}
