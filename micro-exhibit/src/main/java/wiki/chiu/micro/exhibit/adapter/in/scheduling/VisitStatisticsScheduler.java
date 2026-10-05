package wiki.chiu.micro.exhibit.adapter.in.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.exhibit.application.port.in.VisitStatisticsMaintenance;

@Component
public class VisitStatisticsScheduler {

    private final VisitStatisticsMaintenance visitStatisticsMaintenance;

    public VisitStatisticsScheduler(VisitStatisticsMaintenance visitStatisticsMaintenance) {
        this.visitStatisticsMaintenance = visitStatisticsMaintenance;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void rollover() {
        visitStatisticsMaintenance.rollover();
    }
}
