package org.chiu.micro.exhibit.vo;

/**
 * @author mingchiuli
 * @create 2023-04-19 1:50 am
 */
public class VisitStatisticsVo {

    private Long dayVisit;

    private Long weekVisit;

    private Long monthVisit;

    private Long yearVisit;

    VisitStatisticsVo(Long dayVisit, Long weekVisit, Long monthVisit, Long yearVisit) {
        this.dayVisit = dayVisit;
        this.weekVisit = weekVisit;
        this.monthVisit = monthVisit;
        this.yearVisit = yearVisit;
    }

    public static VisitStatisticsVoBuilder builder() {
        return new VisitStatisticsVoBuilder();
    }

    public Long getDayVisit() {
        return this.dayVisit;
    }

    public Long getWeekVisit() {
        return this.weekVisit;
    }

    public Long getMonthVisit() {
        return this.monthVisit;
    }

    public Long getYearVisit() {
        return this.yearVisit;
    }

    public void setDayVisit(Long dayVisit) {
        this.dayVisit = dayVisit;
    }

    public void setWeekVisit(Long weekVisit) {
        this.weekVisit = weekVisit;
    }

    public void setMonthVisit(Long monthVisit) {
        this.monthVisit = monthVisit;
    }

    public void setYearVisit(Long yearVisit) {
        this.yearVisit = yearVisit;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof VisitStatisticsVo)) return false;
        final VisitStatisticsVo other = (VisitStatisticsVo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$dayVisit = this.getDayVisit();
        final Object other$dayVisit = other.getDayVisit();
        if (this$dayVisit == null ? other$dayVisit != null : !this$dayVisit.equals(other$dayVisit)) return false;
        final Object this$weekVisit = this.getWeekVisit();
        final Object other$weekVisit = other.getWeekVisit();
        if (this$weekVisit == null ? other$weekVisit != null : !this$weekVisit.equals(other$weekVisit)) return false;
        final Object this$monthVisit = this.getMonthVisit();
        final Object other$monthVisit = other.getMonthVisit();
        if (this$monthVisit == null ? other$monthVisit != null : !this$monthVisit.equals(other$monthVisit))
            return false;
        final Object this$yearVisit = this.getYearVisit();
        final Object other$yearVisit = other.getYearVisit();
        if (this$yearVisit == null ? other$yearVisit != null : !this$yearVisit.equals(other$yearVisit)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof VisitStatisticsVo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $dayVisit = this.getDayVisit();
        result = result * PRIME + ($dayVisit == null ? 43 : $dayVisit.hashCode());
        final Object $weekVisit = this.getWeekVisit();
        result = result * PRIME + ($weekVisit == null ? 43 : $weekVisit.hashCode());
        final Object $monthVisit = this.getMonthVisit();
        result = result * PRIME + ($monthVisit == null ? 43 : $monthVisit.hashCode());
        final Object $yearVisit = this.getYearVisit();
        result = result * PRIME + ($yearVisit == null ? 43 : $yearVisit.hashCode());
        return result;
    }

    public String toString() {
        return "VisitStatisticsVo(dayVisit=" + this.getDayVisit() + ", weekVisit=" + this.getWeekVisit() + ", monthVisit=" + this.getMonthVisit() + ", yearVisit=" + this.getYearVisit() + ")";
    }

    public static class VisitStatisticsVoBuilder {
        private Long dayVisit;
        private Long weekVisit;
        private Long monthVisit;
        private Long yearVisit;

        VisitStatisticsVoBuilder() {
        }

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
            return new VisitStatisticsVo(this.dayVisit, this.weekVisit, this.monthVisit, this.yearVisit);
        }

        public String toString() {
            return "VisitStatisticsVo.VisitStatisticsVoBuilder(dayVisit=" + this.dayVisit + ", weekVisit=" + this.weekVisit + ", monthVisit=" + this.monthVisit + ", yearVisit=" + this.yearVisit + ")";
        }
    }
}
