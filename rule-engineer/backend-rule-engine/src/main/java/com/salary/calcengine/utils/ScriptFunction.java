package com.salary.calcengine.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;



public class ScriptFunction {

    public static Long square(Long i) {
        return i * i;
    }
    public static Long cubic(Long i) {
        return i * i * i;
    }
    public static Long sum(Long... i) {
        return Arrays.stream(i).reduce(0L, (a, b) -> a + b);
    }

    public static LocalDate dateValue(String text) throws ScriptFunctionException {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if(StringUtils.isNotEmpty(text)){
            try {
                return LocalDate.parse(text, format);
            }
            catch ( Exception ex) {
                throw new ScriptFunctionException("date format error");
            }
        }
        else{
            //TODO: 空字符串处理
            return LocalDate.of(2099,12,31);
        }
    }

    public static String dateFormat(LocalDate dt, String pattern) {
        if (dt != null) {
            DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
            return format.format(dt);
        }
        return "";
    }

    public static String concatenate(String... str){
        return Arrays.stream(str).reduce("", (a, b) -> a + b);
    }

    public static LocalDate eoMonth(LocalDate dt, int days) throws ScriptFunctionException {
        if(days <0) {
            throw new ScriptFunctionException("days must greater or equals than 0");
        }

        if(dt!=null) {

            // 获取该月的最后一天
            LocalDate lastDayOfMonth = dt.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

            // 计算倒数第 days 天
            return lastDayOfMonth.minus(days, ChronoUnit.DAYS);

        }
        return null;
    }

    public static int workdays(LocalDate startDate, LocalDate endDate) {
        int workDaysCount = 0;

        // 遍历两个日期之间的每一天
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 判断是否为工作日（排除周六和周日）
            if (!date.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                workDaysCount++;
            }
        }

        return workDaysCount;
    }

    public static Object IF(boolean cond, Object a, Object b){
        if(cond) {
            return a;
        } else {
            return b;
        }
    }

    public static LocalDate dateAdd(LocalDate dt, int days){
        if(dt!=null) {
            return dt.plusDays(days);
        }
        return null;
    }

}

