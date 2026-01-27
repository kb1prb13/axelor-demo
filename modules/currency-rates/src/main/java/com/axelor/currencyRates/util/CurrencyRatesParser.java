package com.axelor.currencyRates.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class CurrencyRatesParser {
    private static final Logger LOG = LoggerFactory.getLogger(CurrencyRatesParser.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static ParsedRates parseRates(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            Element root = doc.getDocumentElement();

            LocalDate rateDate = parseDate(root.getAttribute("Date"));
            if (rateDate == null) {
                rateDate = parseDate(findChildText(root, "Date"));
            }
            if (rateDate == null) {
                rateDate = LocalDate.now();
            }

            NodeList currencyNodes = firstNonEmptyNodeList(root);
            List<RateItem> items = new ArrayList<>();

            for (int i = 0; i < currencyNodes.getLength(); i++) {
                Node node = currencyNodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                String code = firstNonBlank(element.getAttribute("ISOCode"), findChildText(element, "ISOCode"));
                if (isBlank(code)) {
                    continue;
                }

                String currencyCode = element.getAttribute("ISOCode");
                String currencyName = Currency.getInstance(currencyCode).getDisplayName();
                if (isBlank(currencyName)) {
                    currencyName = code;
                }

                Integer nominal = parseInteger(
                        firstNonBlank(
                                element.getAttribute("Nominal"),
                                findChildText(element, "Nominal")));
                if (nominal == null || nominal <= 0) {
                    nominal = 1;
                }

                BigDecimal rate = parseBigDecimal(
                        firstNonBlank(
                                element.getAttribute("Value"),
                                findChildText(element, "Value")));
                if (rate == null) {
                    continue;
                }

                LocalDate itemDate =
                        parseDate(firstNonBlank(element.getAttribute("Date"), element.getAttribute("date")));
                if (itemDate == null) {
                    itemDate = rateDate;
                }

                items.add(new RateItem(code, currencyName, nominal, rate, itemDate));
            }

            return new ParsedRates(rateDate, items);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse NBKR XML payload", ex);
        }
    }

    private static NodeList firstNonEmptyNodeList(Element root) {
        for (String tag : new String[]{"Currency", "Valute", "Rate"}) {
            NodeList list = root.getElementsByTagName(tag);
            if (list != null && list.getLength() > 0) {
                return list;
            }
        }
        return root.getElementsByTagName("*");
    }

    private static LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        String candidate = value.trim();

        try {
            return LocalDate.parse(candidate, DATE_TIME_FORMATTER.withLocale(Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {
            LOG.error("Failed to parse date '{}'", candidate);
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static Integer parseInteger(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal parseBigDecimal(String value) {
        if (isBlank(value)) {
            return null;
        }
        String normalized = value.replace("\u00A0", "").replace(" ", "").replace(",", ".").trim();
        if (isBlank(normalized)) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String findChildText(Element parent, String tags) {
        NodeList nodes = parent.getElementsByTagName(tags);
        if (nodes != null && nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent();
            if (isBlank(text)) {
                return text;
            }
        }
        return null;
    }

    public record ParsedRates(LocalDate rateDate, List<RateItem> items) {
    }

    public record CurrencyRateImportResult(int total, int created, int updated, LocalDate rateDate) {
    }

    public record RateItem(String code, String name, int nominal, BigDecimal rate, LocalDate date) {
    }
}
