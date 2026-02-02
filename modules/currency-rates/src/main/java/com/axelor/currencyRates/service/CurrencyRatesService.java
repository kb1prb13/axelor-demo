/*
 * SPDX-FileCopyrightText: Axelor <https://axelor.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.axelor.currencyRates.service;

import com.axelor.currencyRates.db.CurrencyRate;
import com.axelor.currencyRates.db.repo.CurrencyRateRepository;
import com.axelor.currencyRates.util.CurrencyRatesParser.CurrencyRateImportResult;
import com.axelor.currencyRates.util.pojo.CurrencyRates;
import com.axelor.currencyRates.util.pojo.Rates;
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
        CurrencyRates parsedRates = parseRates(xml);

        int created = 0;
        int updated = 0;

        for (int i = 0; i < parsedRates.getCurrencies().size(); i++) {
            Rates item = parsedRates.getCurrencies().get(i);
            CurrencyRate existing =
                    currencyRateRepository
                            .all()
                            .filter("self.code = ?1 AND self.pullDate = ?2", item.getIsoCode(), item.getPullDate())
                            .fetchOne();

            if (existing == null) {
                existing = new CurrencyRate();
                created++;
            } else {
                updated++;
            }

            existing.setCode(item.getIsoCode());
            existing.setName(item.getName());
            existing.setNominal(item.getNominal());
            existing.setRate(item.getRate());
            existing.setPullDate(item.getPullDate());
            currencyRateRepository.save(existing);
        }

        LOG.info(
                "NBKR rates processed: total={}, created={}, updated={}, date={}",
                parsedRates.getCurrencies().size(),
                created,
                updated,
                parsedRates.getDate());

        return new CurrencyRateImportResult(parsedRates.getCurrencies().size(), created, updated, parsedRates.getDate());
    }
}
