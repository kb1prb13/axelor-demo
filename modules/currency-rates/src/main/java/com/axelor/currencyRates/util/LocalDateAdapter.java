package com.axelor.currencyRates.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public String marshal(LocalDate localDate) {
        return localDate.format(dateFormat);
    }

    @Override
    public LocalDate unmarshal(String localDate) {
        return LocalDate.parse(localDate, dateFormat);
    }

}
