package com.axelor.currencyRates.util.pojo;

import com.axelor.currencyRates.util.BigDecimalAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.math.BigDecimal;
import java.time.LocalDate;

@XmlRootElement(name = "Currency")
@XmlAccessorType(XmlAccessType.FIELD)
public class Rates {
    @XmlAttribute(name = "ISOCode")
    private String isoCode;

    @XmlElement(name = "Nominal")
    private int nominal;

    @XmlElement(name = "Value")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    private BigDecimal rate;

    @XmlTransient
    private String name;

    @XmlTransient
    private LocalDate pullDate;

    public Rates() {
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public int getNominal() {
        return nominal;
    }

    public void setNominal(int nominal) {
        this.nominal = nominal;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getPullDate() {
        return pullDate;
    }

    public void setPullDate(LocalDate pullDate) {
        this.pullDate = pullDate;
    }
}
