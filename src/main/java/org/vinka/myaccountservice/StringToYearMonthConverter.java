package org.vinka.myaccountservice;


import org.springframework.core.convert.converter.Converter;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class StringToYearMonthConverter implements Converter<String, YearMonth> {
    @Override
    public YearMonth convert(String value) {
        return YearMonth.parse(value, DateTimeFormatter.ofPattern("MM-yyyy"));
    }
}
