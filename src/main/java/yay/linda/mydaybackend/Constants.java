package yay.linda.mydaybackend;

import yay.linda.mydaybackend.entity.Day;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Constants {

    public static final DateTimeFormatter YEAR_MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    public static final int BCRYPT_LOG_ROUNDS = 10;

    public static final List<String> HOUR_ORDER = Arrays.asList(
            "12 AM", "01 AM", "02 AM", "03 AM", "04 AM", "05 AM", "06 AM", "07 AM", "08 AM", "09 AM", "10 AM", "11 AM",
            "12 PM", "01 PM", "02 PM", "03 PM", "04 PM", "05 PM", "06 PM", "07 PM", "08 PM", "09 PM", "10 PM", "11 PM");

    public static final List<String> WEEK_NUM_ORDER = Arrays.asList(
            "Week 1", "Week 2", "Week 3", "Week 4", "Week 5");

    public static final List<String> WEEKDAYS_ORDER = Arrays.asList(
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");

    public static final List<String> MONTHS_ORDER = Arrays.asList(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    public static final String DAY_KEY = "day";

    public static final String WEEK_KEY = "week";

    public static final String MONTH_KEY = "month";

    public static final String YEAR_KEY = "year";

    public static final String COUNTS_KEY = "counts";

    public static final String RECORDS_KEY = "records";

    public static List<Day> getLastSevenDays(List<Day> days) {
        if (days.size() <= 7) {
            return days;
        } else {
            return days.subList(0, 7);
        }
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

        if (localDate.getDayOfMonth() <= 7) {
            weekStartLabel = "Week 1";
        } else if (localDate.getDayOfMonth() <= 14) {
            weekStartLabel = "Week 2";
        } else if (localDate.getDayOfMonth() <= 21) {
            weekStartLabel = "Week 3";
        } else if (localDate.getDayOfMonth() <= 28) {
            weekStartLabel = "Week 4";
        } else {
            weekStartLabel = "Week 5";
        }

        return weekStartLabel;
    }
}
