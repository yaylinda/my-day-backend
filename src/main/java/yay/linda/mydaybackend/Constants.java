package yay.linda.mydaybackend;

import yay.linda.mydaybackend.entity.Day;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Constants {

    public static final DateTimeFormatter YEAR_MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final List<String> HOUR_ORDER = Arrays.asList(
            "12 AM", "01 AM", "02 AM", "03 AM", "04 AM", "05 AM", "06 AM", "07 AM", "08 AM", "09 AM", "10 AM", "11 AM",
            "12 PM", "01 PM", "02 PM", "03 PM", "04 PM", "05 PM", "06 PM", "07 PM", "08 PM", "09 PM", "10 PM", "11 PM");

    public static final List<String> WEEK_NUM_ORDER = Arrays.asList(
            "Week 1", "Week 2", "Week 3", "Week 4", "Week 5");

    public static final List<String> WEEKDAYS_ORDER = Arrays.asList(
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");

    public static final List<String> MONTHS_ORDER = Arrays.asList(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    public static List<Day> getWeek(List<Day> days) {
        LocalDate lastSunday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        List<Day> week = new ArrayList<>();

        for (int i = days.size() - 1; i >= 0; i--) {
            LocalDate latestDate = LocalDate.parse(days.get(i).getDate());
            if (latestDate.isAfter(lastSunday) || latestDate.isEqual(lastSunday)) {
                week.add(days.get(i));
            }
        }

        return week;
    }

    public static List<Day> getMonth(List<Day> days) {
        List<Day> month = new ArrayList<>();
        String currentMonthStr = days.get(0).getDate().substring(5, 7); // YYYY-MM-DD

        for (Day day : days) {
            if (day.getDate().substring(5, 7).equals(currentMonthStr)) {
                month.add(day);
            }
            if (day.getDate().substring(8, 10).equals("01")) {
                break;
            }
        }

        return month;
    }

    public static List<Day> getYear(List<Day> days) {
        List<Day> year = new ArrayList<>();
        String currentYearStr = days.get(0).getDate().substring(0, 4);

        for (Day day : days) {
            if (day.getDate().substring(0, 4).equals(currentYearStr)) {
                year.add(day);
            }
            if (day.getDate().equals(String.format("%s-01-01", currentYearStr))) {
                break;
            }
        }

        return year;
    }

    public static String determineWeekStartLabel(LocalDate localDate) {
        String weekStartLabel = "";

        if (localDate.getDayOfMonth() >= 1 && localDate.getDayOfMonth() <= 7) {
            weekStartLabel = "Week 1";
        } else if (localDate.getDayOfMonth() >= 8 && localDate.getDayOfMonth() <= 14) {
            weekStartLabel = "Week 2";
        } else if (localDate.getDayOfMonth() >= 15 && localDate.getDayOfMonth() <= 21) {
            weekStartLabel = "Week 3";
        } else if (localDate.getDayOfMonth() >= 22 && localDate.getDayOfMonth() <= 28) {
            weekStartLabel = "Week 4";
        } else {
            weekStartLabel = "Week 5";
        }

        return weekStartLabel;
    }
}
