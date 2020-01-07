package yay.linda.mydaybackend;

import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.ChartData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        LocalDate minDate = LocalDate.parse(days.get(days.size() - 1).getDate()).minusDays(1);

        while (days.size() < 7) {
            days.add(new Day(minDate.format(YEAR_MONTH_DAY_FORMATTER),days.get(0).getUsername()));
            minDate = minDate.minusDays(1);
        }

        return days.subList(0, 7);
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

    public static void aggregateScoreByLabel(Map<String, List<Integer>> map, ChartData chartData) {
        map.forEach((k, v) ->
                chartData.getLabelsDataMap()
                        .put(k, Arrays.stream(v.toArray())
                                .mapToInt(i -> (Integer) i)
                                .average()
                                .orElse(0.0)));
    }

    public static void aggregateActivityByLabel(
            Map<String, List<String>> map,
            ChartData chartData,
            Set<String> uniqueActivities) {

        chartData.setLegend(new ArrayList<>(uniqueActivities));

        map.forEach((k, v) -> {

            List<Integer> activitiesCount;

            if (v.isEmpty()) {
                int[] temp = new int[chartData.getLegend().size()];
                Arrays.fill(temp, 0);
                activitiesCount = Arrays.stream(temp).boxed().collect(Collectors.toList());
            } else {
                Map<String, Long> activityCountMap = v.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                long[] temp = new long[chartData.getLegend().size()];
                for (int i = 0; i < chartData.getLegend().size(); i++) {
                    String activityName = chartData.getLegend().get(i);
                    temp[i] = activityCountMap.getOrDefault(activityName, 0L);
                }

                activitiesCount = Arrays.stream(temp).boxed().map(Long::intValue).collect(Collectors.toList());
            }

            chartData.getLabelsDataMap().put(k, activitiesCount);
        });
    }
}
