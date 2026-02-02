package com.axelor.currencyRates.util;

import com.axelor.currencyRates.util.pojo.CurrencyRates;
import com.axelor.currencyRates.util.pojo.Rates;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class CurrencyRatesParser {
    public static CurrencyRates parseRates(String xml) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(CurrencyRates.class);
            Unmarshaller um = ctx.createUnmarshaller();

            CurrencyRates rates = (CurrencyRates) um.unmarshal(new InputSource(new StringReader(xml)));

            List<Rates> ratesList = new ArrayList<>();

            for (Rates cur : rates.getCurrencies()) {
                cur.setName(Currency.getInstance(cur.getIsoCode()).getDisplayName());
                if (isBlank(cur.getName())) {
                    cur.setName(cur.getIsoCode());
                }

                if (cur.getRate() == null) {
                    cur.setRate(BigDecimal.ZERO);
                }

                cur.setPullDate(rates.getDate());

                ratesList.add(cur);
            }

            return new CurrencyRates(rates.getDate(), ratesList);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record CurrencyRateImportResult(int total, int created, int updated, LocalDate rateDate) {
    }
}
