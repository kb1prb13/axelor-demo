package com.axelor.currencyRates.util.pojo;

import com.axelor.currencyRates.util.LocalDateAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.List;

@XmlRootElement(name = "CurrencyRates")
@XmlAccessorType(XmlAccessType.FIELD)
public class CurrencyRates {
    @XmlAttribute(name = "Date")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate date;

    @XmlElement(name = "Currency")
    private List<Rates> currencies;

    public CurrencyRates() {
    }

    public CurrencyRates(LocalDate date, List<Rates> currencies) {
        this.date = date;
        this.currencies = currencies;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Rates> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Rates> currencies) {
        this.currencies = currencies;
    }
}
