package com.axelor.currencyRates.job;

import com.axelor.currencyRates.service.CurrencyRatesPullerService;
import com.axelor.currencyRates.service.CurrencyRatesService;
import com.google.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrencyRatesRefresherJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyRatesRefresherJob.class);

    @Inject
    private CurrencyRatesService currencyRatesService;

    @Inject
    CurrencyRatesPullerService currencyRatesPullerService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            LOG.info("Starting CurrencyRatesRefresherJob to pull currency rates from NBKR.");
            String fetchedXml = currencyRatesPullerService.fetchDailyRates();
            currencyRatesService.insertDailyRates(fetchedXml);
        } catch (Exception ex) {
            LOG.error("Failed to update NBKR daily rates", ex);
            throw new JobExecutionException(ex);
        }
    }
}
