package wiki.chiu.micro.exhibit.application.port.in;

/**
 * Rolls the visit counters over at the day, week, month, and year boundaries.
 */
public interface VisitStatisticsMaintenance {

    /**
     * Performs the rollover that is due for the current date, at most once per day across every
     * replica.
     */
    void rollover();
}
