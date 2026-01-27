/*
 * SPDX-FileCopyrightText: Axelor <https://axelor.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.axelor.currencyRates.service;

import com.axelor.currencyRates.db.CurrencyRate;
import com.axelor.currencyRates.db.repo.CurrencyRateRepository;
import com.axelor.currencyRates.util.CurrencyRatesParser.CurrencyRateImportResult;
import com.axelor.currencyRates.util.CurrencyRatesParser.ParsedRates;
import com.axelor.currencyRates.util.CurrencyRatesParser.RateItem;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.axelor.currencyRates.util.CurrencyRatesParser.parseRates;

@Singleton
public class CurrencyRatesService {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyRatesService.class);

    private final CurrencyRateRepository currencyRateRepository;

    @Inject
    public CurrencyRatesService(CurrencyRateRepository currencyRateRepository) {
        this.currencyRateRepository = currencyRateRepository;
    }

    @Transactional
    public CurrencyRateImportResult insertDailyRates(String xml) {
        ParsedRates parsedRates = parseRates(xml);

        int created = 0;
        int updated = 0;

        for (int i = 0; i < parsedRates.items().size(); i++) {
            RateItem item = parsedRates.items().get(i);
            CurrencyRate existing =
                    currencyRateRepository
                            .all()
                            .filter("self.code = ?1 AND self.pullDate = ?2", item.code(), item.date())
                            .fetchOne();

            if (existing == null) {
                existing = new CurrencyRate();
                created++;
            } else {
                updated++;
            }

            existing.setCode(item.code());
            existing.setName(item.name());
            existing.setNominal(item.nominal());
            existing.setRate(item.rate());
            existing.setPullDate(item.date());
            currencyRateRepository.save(existing);
        }

        LOG.info(
                "NBKR rates processed: total={}, created={}, updated={}, date={}",
                parsedRates.items().size(),
                created,
                updated,
                parsedRates.rateDate());

        return new CurrencyRateImportResult(parsedRates.items().size(), created, updated, parsedRates.rateDate());
    }
}
