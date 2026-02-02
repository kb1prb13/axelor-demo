package com.axelor.currencyRates.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class BigDecimalAdapter extends XmlAdapter<String, BigDecimal> {
    private static final Logger LOG = LoggerFactory.getLogger(BigDecimalAdapter.class);

    @Override
    public BigDecimal unmarshal(String v) throws Exception {
        if (v == null) return BigDecimal.ZERO;
        return new BigDecimal(v.replace(",", "."));
    }

    @Override
    public String marshal(BigDecimal v) throws Exception {
        if (v == null) return "";
        return v.toPlainString().replace(".", ",");
    }
}
