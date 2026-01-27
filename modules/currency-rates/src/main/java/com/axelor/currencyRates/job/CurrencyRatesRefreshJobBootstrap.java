package com.axelor.currencyRates.job;

import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.db.repo.MetaScheduleRepository;
import com.axelor.quartz.JobRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
public class CurrencyRatesRefreshJobBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyRatesRefreshJobBootstrap.class);
    private static final String SCHEDULE_NAME = "NBKR Daily Currency Rates";
    private static final String CRON_DAILY_0900 = "0 0 9 * * ?";

    private final MetaScheduleRepository metaScheduleRepository;
    private final JobRunner jobRunner;

    @Inject
    public CurrencyRatesRefreshJobBootstrap(
            MetaScheduleRepository metaScheduleRepository, JobRunner jobRunner) {
        this.metaScheduleRepository = metaScheduleRepository;
        this.jobRunner = jobRunner;
    }

    @PostConstruct
    @Transactional
    public void ensureScheduleExists() {
        MetaSchedule schedule =
                metaScheduleRepository.all().filter("self.name = ?1", SCHEDULE_NAME).fetchOne();
        boolean changed = false;

        if (schedule == null) {
            schedule = new MetaSchedule();
            schedule.setName(SCHEDULE_NAME);
            schedule.setDescription("Fetch NBKR daily currency rates at 09:00");
            schedule.setActive(true);
            changed = true;
        }

        String jobClass = CurrencyRatesRefresherJob.class.getName();
        if (!Objects.equals(schedule.getJob(), jobClass)) {
            schedule.setJob(jobClass);
            changed = true;
        }
        if (!Objects.equals(schedule.getCron(), CRON_DAILY_0900)) {
            schedule.setCron(CRON_DAILY_0900);
            changed = true;
        }
        if (!Boolean.TRUE.equals(schedule.getActive())) {
            schedule.setActive(true);
            changed = true;
        }

        if (changed) {
            metaScheduleRepository.save(schedule);
            if (jobRunner.isEnabled()) {
                try {
                    jobRunner.update(schedule);
                } catch (Exception ex) {
                    LOG.warn("NBKR schedule saved but scheduler update failed", ex);
                }
            } else {
                LOG.info("NBKR schedule saved; quartz scheduler is disabled");
            }
        }
    }
}
