package com.jh.dividendpj.scraper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Month {
    JAN("Jan", 1),
    FEB("Feb", 2),
    MAR("Mar", 3),
    APR("Apr", 4),
    MAY("May", 5),
    JUN("Jun", 6),
    JUL("Jul", 7),
    AUG("Aug", 8),
    SEP("Sep", 9),
    OCT("Oct", 10),
    NOV("Nov", 11),
    DEC("Dec", 12);

    private String month;
    private int monthToNumber;

    // str -> int of month
    public static int stringToMonth(String str) {
        for (Month value : Month.values()) {
            if (str.equals(value.month)) {
                return value.monthToNumber;
            }
        }
        return -1;
    }
}
