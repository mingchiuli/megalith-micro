package wiki.chiu.micro.exhibit.domain;

/**
 * The key that hands the daily visit rollover to exactly one replica.
 */
public final class VisitStatisticsKeys {

    public static final String DAILY_ROLLOVER_CLAIM = "statisticsFinishKey";

    private VisitStatisticsKeys() {
    }
}
