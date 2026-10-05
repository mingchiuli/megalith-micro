package wiki.chiu.micro.exhibit.application.service;

import java.time.Clock;
import java.time.LocalDateTime;

import wiki.chiu.micro.exhibit.application.port.in.VisitStatisticsMaintenance;
import wiki.chiu.micro.exhibit.application.port.out.VisitStatisticsStore;

/**
 * Clears the visit counters when their period ends. Every replica runs the same schedule, so the
 * store hands the rollover to exactly one of them per day.
 */
public class VisitStatisticsMaintenanceService implements VisitStatisticsMaintenance {

    private static final int ROLLOVER_HOUR = 0;
    private static final int FIRST_DAY_OF_WEEK = 1;
    private static final int FIRST_DAY_OF_MONTH = 1;
    private static final int FIRST_DAY_OF_YEAR = 1;

    private final VisitStatisticsStore store;

    private final Clock clock;

    public VisitStatisticsMaintenanceService(VisitStatisticsStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    @Override
    public void rollover() {
        LocalDateTime now = LocalDateTime.now(clock);
        if (now.getHour() != ROLLOVER_HOUR || !store.claimDailyRollover()) {
            return;
        }

        store.clearDailyVisits();
        if (now.getDayOfWeek().getValue() == FIRST_DAY_OF_WEEK) {
            store.clearWeeklyVisits();
            store.clearHotRead();
        }
        if (now.getDayOfMonth() == FIRST_DAY_OF_MONTH) {
            store.clearMonthlyVisits();
        }
        if (now.getDayOfYear() == FIRST_DAY_OF_YEAR) {
            store.clearYearlyVisits();
        }
    }
}
