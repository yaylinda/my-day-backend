package yay.linda.mydaybackend;

import java.time.format.DateTimeFormatter;
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
}
