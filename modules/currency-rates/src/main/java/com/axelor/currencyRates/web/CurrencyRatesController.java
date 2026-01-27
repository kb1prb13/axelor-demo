/*
 * SPDX-FileCopyrightText: Axelor <https://axelor.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.axelor.currencyRates.web;

import com.axelor.currencyRates.service.CurrencyRatesService;
import com.axelor.currencyRates.service.CurrencyRatesPullerService;
import com.axelor.currencyRates.util.CurrencyRatesParser.CurrencyRateImportResult;
import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class CurrencyRatesController {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyRatesController.class);

    @Inject
    private CurrencyRatesService currencyRatesService;

    @Inject
    private CurrencyRatesPullerService currencyRatesPullerService;

    @CallMethod
    public void refreshRates(ActionRequest request, ActionResponse response) {
        try {
            String fetchedXml = currencyRatesPullerService.fetchDailyRates();
            CurrencyRateImportResult result = currencyRatesService.insertDailyRates(fetchedXml);
            response.setInfo(
                    String.format(
                            "NBKR rates updated for %s: total %d, created %d, updated %d.",
                            result.rateDate(), result.total(), result.created(), result.updated()));
        } catch (Exception ex) {
            LOG.error("Manual NBKR update failed", ex);
            response.setError("NBKR update failed. Check logs for details.");
        }
    }
}
