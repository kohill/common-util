package com.healthedge.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

public class DefaultDateTimeUtil {

    public static final String DATE_ONLY_FORMAT_PATTERN = "yyyy-MM-dd";
    public static final String DATE_ONLY_FORMAT_PATTERN_DD_LLL_YY = "dd-LLL-yy";
    public static final String DATE_TIME_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String DATE_TIME_NO_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final DateTimeFormatter TIMESETTER_HTTP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    public static final DateTimeFormatter DATE_FORMATTER_UI = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_UI = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_hhmmss");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_12_HR = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");
    public static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern(DATE_ONLY_FORMAT_PATTERN);//name taken from base
    public static final DateTimeFormatter DATE_DD_LLL_YY = DateTimeFormatter.ofPattern(DATE_ONLY_FORMAT_PATTERN_DD_LLL_YY);
    public static final DateTimeFormatter DATE_MM_DD_YYYY = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    public static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("MMddyyyy");
    public static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HHmmss");
    public static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_IN_DXP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final DateTimeFormatter FILE_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static LocalDate parseDate(CharSequence text, DateTimeFormatter formatter) {
        return LocalDate.parse(text, formatter);
    }

    public static LocalDate parseDate(CharSequence text) {
        return LocalDate.parse(text, DATE_FORMATTER_UI);
    }

    public static LocalDateTime toLocalDateTime(LocalDate localDate) {
        return LocalDateTime.of(localDate,  LocalTime.of(8, 0, 0));
    }


    public static LocalDate getClosestPreviousWorkingDay(LocalDate localDate) {
        if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return localDate.minusDays(1);
        }
        if (localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return localDate.minusDays(2);
        }
        return localDate;
    }

    public static LocalDate getClosestNextWorkingDay(LocalDate localDate) {
        if (localDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return localDate.plusDays(2);
        }
        if (localDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return localDate.plusDays(1);
        }
        return localDate;
    }

    public static boolean isWeekend(Temporal localDate) {
        DayOfWeek day = DayOfWeek.of(localDate.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
