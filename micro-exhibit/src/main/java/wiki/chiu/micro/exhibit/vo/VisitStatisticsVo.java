package wiki.chiu.micro.exhibit.vo;

/**
 * @author mingchiuli
 * @create 2023-04-19 1:50 am
 */
public record VisitStatisticsVo(

        Long dayVisit,

        Long weekVisit,

        Long monthVisit,

        Long yearVisit) {

    public static VisitStatisticsVoBuilder builder() {
        return new VisitStatisticsVoBuilder();
    }

    public static class VisitStatisticsVoBuilder {
        private Long dayVisit;
        private Long weekVisit;
        private Long monthVisit;
        private Long yearVisit;


        public VisitStatisticsVoBuilder dayVisit(Long dayVisit) {
            this.dayVisit = dayVisit;
            return this;
        }

        public VisitStatisticsVoBuilder weekVisit(Long weekVisit) {
            this.weekVisit = weekVisit;
            return this;
        }

        public VisitStatisticsVoBuilder monthVisit(Long monthVisit) {
            this.monthVisit = monthVisit;
            return this;
        }

        public VisitStatisticsVoBuilder yearVisit(Long yearVisit) {
            this.yearVisit = yearVisit;
            return this;
        }

        public VisitStatisticsVo build() {
            return new VisitStatisticsVo(dayVisit, weekVisit, monthVisit, yearVisit);
        }
    }
}
