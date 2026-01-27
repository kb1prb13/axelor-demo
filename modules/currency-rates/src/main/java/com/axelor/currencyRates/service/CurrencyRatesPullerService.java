/*
 * SPDX-FileCopyrightText: Axelor <https://axelor.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.axelor.currencyRates.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Singleton
public class CurrencyRatesPullerService {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyRatesPullerService.class);
    private static final String NBKR_DAILY_URL = "https://www.nbkr.kg/XML/daily.xml";

    private final HttpClient httpClient;

    @Inject
    public CurrencyRatesPullerService() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    }

    @Transactional
    public String fetchDailyRates() {
        LOG.info("Fetching NBKR daily currency rates from {}", NBKR_DAILY_URL);

        try {
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(CurrencyRatesPullerService.NBKR_DAILY_URL))
                            .timeout(Duration.ofSeconds(30))
                            .header("Accept", "application/xml")
                            .GET()
                            .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "NBKR responded with status " + response.statusCode() + " for " + CurrencyRatesPullerService.NBKR_DAILY_URL);
            }

            return response.body();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to fetch NBKR daily rates", ex);
        }
    }
}
