package wiki.chiu.micro.exhibit.application.port.out;

/**
 * Owns the visit counters and the claim that makes the daily rollover happen exactly once per day
 * across every replica.
 */
public interface VisitStatisticsStore {

    /**
     * Atomically claims the rollover of the current day.
     *
     * @return true when this replica owns the rollover and must perform it
     */
    boolean claimDailyRollover();

    void clearDailyVisits();

    void clearWeeklyVisits();

    void clearMonthlyVisits();

    void clearYearlyVisits();

    void clearHotRead();
}
