package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.ChartData;
import yay.linda.mydaybackend.model.StatsDTO;
import yay.linda.mydaybackend.model.StatsType;
import yay.linda.mydaybackend.repository.DayRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.format.TextStyle.SHORT_STANDALONE;
import static yay.linda.mydaybackend.Constants.YEAR_MONTH_DAY_FORMATTER;

@Service
public class StatsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class);

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private SessionService sessionService;

    public StatsDTO getStats(String statsType, String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        List<Day> days = dayRepository.findByUsernameOrderByDateDesc(username);

        StatsType type = StatsType.valueOf(statsType);

        switch (type) {
            case SCORE:
                return calculateScoreStats(days);
            case ACTIVITY:
                return calculateActivityStats(days);
            case PROMPT:
                return calculatePromptStats(days);
            case SUMMARY:
                return calculateSummaryStats(days);
        }

        return new StatsDTO();
    }

    private StatsDTO calculateScoreStats(List<Day> days) {
        LOGGER.info("Calculating Score stats from {} days' data", days.size());

        StatsDTO statsDTO = new StatsDTO();

        // get stats data for latest day, avg by hour
        statsDTO.setDay(calculateScoreStatsForDayByHour(days.get(0)));

        // get stats data for last 7 days, avg by day
        LocalDate minDate = LocalDate.parse(days.get(days.size() - 1).getDate()).minusDays(1);
        while (days.size() < 7) {
            days.add(new Day(minDate.format(YEAR_MONTH_DAY_FORMATTER),days.get(0).getUsername()));
            minDate = minDate.minusDays(1);
        }

        statsDTO.setWeek(calculateScoreStatsForWeekByDay(days.subList(0, 7)));

        // get stats data for current month, avg by day (week?)
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

        statsDTO.setMonth(calculateScoreStatsForMonthByWeek(month));

        // get stats data for current year, avg by month
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

        statsDTO.setYear(calculateScoreStatsForYearByMonth(year));

        return statsDTO;
    }

    private ChartData calculateScoreStatsForDayByHour(Day day) {
        LOGGER.info("Using latest date [{}], to calculate average scores for DAY (per hour)", day.getDate());

        ChartData dayChartData = ChartData.dayChartData();

        Map<String, List<Integer>> hourToValueMapping = new HashMap<>();

        dayChartData.getLabels().forEach((hourLabel) -> day.getEmotions().forEach((emotionScore) -> {
            if (hourLabel.equals(String.format("%s %s",
                    emotionScore.getStartTime().substring(0, 2), emotionScore.getStartTime().substring(6, 8)))) {
                if (!hourToValueMapping.containsKey(hourLabel)) {
                    hourToValueMapping.put(hourLabel, new ArrayList<>());
                }
                hourToValueMapping.get(hourLabel).add(emotionScore.getEmotionScore());
            } else {
                hourToValueMapping.put(hourLabel, new ArrayList<>());
            }
        }));

        hourToValueMapping.forEach((k, v) -> {
            Double value;
            if (v.isEmpty()) {
                value = 0.0;
            } else {
                value = Arrays.stream(v.toArray()).mapToInt(i -> (Integer) i).average().orElse(0.0);
            }
            dayChartData.getLabelsDataMap().put(k, value);
        });

        return dayChartData;
    }

    private ChartData calculateScoreStatsForWeekByDay(List<Day> week) {
        LOGGER.info("Using dates [{}-{}], to calculate average scores for WEEK (per day)",
                week.get(week.size() - 1), week.get(0));

        ChartData dayChartData = ChartData.weekChartData();

        Map<String, List<Integer>> weekdayLabelToScoreMapping = new HashMap<>();

        week.forEach(d -> {
            String weekday = LocalDate.parse(d.getDate())
                    .getDayOfWeek().getDisplayName(SHORT_STANDALONE, Locale.ENGLISH);
            if (d.getEmotions().isEmpty()) {
                weekdayLabelToScoreMapping.put(weekday, new ArrayList<>());
            } else {
                List<Integer> dayEmotions = new ArrayList<>();
                d.getEmotions().forEach(e -> dayEmotions.add(e.getEmotionScore()));
                weekdayLabelToScoreMapping.put(weekday, dayEmotions);
            }
        });

        weekdayLabelToScoreMapping
                .forEach((k, v) -> dayChartData.getLabelsDataMap()
                        .put(k, Arrays.stream(v.toArray())
                                .mapToInt(i -> (Integer) i).
                                        average()
                                .orElse(0.0)));

        return dayChartData;
    }

    private ChartData calculateScoreStatsForMonthByWeek(List<Day> month) {
        LOGGER.info("Using [{}-{}], to calculate average scores for MONTH (per week)",
                month.get(month.size() - 1).getDate(), month.get(0));

        LocalDate localDate = LocalDate.parse(month.get(0).getDate());

        ChartData dayChartData = ChartData.monthChartData(
                localDate.getMonth().getValue(),
                localDate.isLeapYear());

        Map<String, List<Integer>> weekStartLabelToScoreMapping = new HashMap<>();

        month.forEach(d -> {

            String weekStartLabel = "";
            if (localDate.getDayOfMonth() >= 1 && localDate.getDayOfMonth() <= 7) {
                weekStartLabel = String.format("%d-01", localDate.getMonth().getValue());
            } else if (localDate.getDayOfMonth() >= 8 && localDate.getDayOfMonth() <= 14) {
                weekStartLabel = String.format("%d-08", localDate.getMonth().getValue());;
            } else if (localDate.getDayOfMonth() >= 15 && localDate.getDayOfMonth() <= 21) {
                weekStartLabel = String.format("%d-15", localDate.getMonth().getValue());;
            } else if (localDate.getDayOfMonth() >= 22 && localDate.getDayOfMonth() <= 28) {
                weekStartLabel = String.format("%d-22", localDate.getMonth().getValue());;
            } else {
                weekStartLabel = String.format("%d-29", localDate.getMonth().getValue());;
            }

            if (d.getEmotions().isEmpty()) {
                weekStartLabelToScoreMapping.put(weekStartLabel, new ArrayList<>());
            } else {
                List<Integer> weekEmotions = new ArrayList<>();
                d.getEmotions().forEach(e -> weekEmotions.add(e.getEmotionScore()));
                weekStartLabelToScoreMapping.put(weekStartLabel, weekEmotions);
            }
        });

        weekStartLabelToScoreMapping
                .forEach((k, v) -> dayChartData.getLabelsDataMap()
                        .put(k, Arrays.stream(v.toArray())
                                .mapToInt(i -> (Integer) i).
                                        average()
                                .orElse(0.0)));

        return dayChartData;
    }

    private ChartData calculateScoreStatsForYearByMonth(List<Day> year) {
        LOGGER.info("Using [{}-{}], to calculate average scores for YEAR (per month)",
                year.get(year.size() - 1).getDate(), year.get(0));

        LocalDate localDate = LocalDate.parse(year.get(0).getDate());

        ChartData dayChartData = ChartData.yearChartData();

        Map<String, List<Integer>> monthLabelToStartMapping = new HashMap<>();

        year.forEach(d -> {
            if (d.getEmotions().isEmpty()) {
                monthLabelToStartMapping.put(
                        localDate.getMonth().getDisplayName(SHORT_STANDALONE, Locale.ENGLISH),
                        new ArrayList<>());
            } else {
                List<Integer> weekEmotions = new ArrayList<>();
                d.getEmotions().forEach(e -> weekEmotions.add(e.getEmotionScore()));
                monthLabelToStartMapping.put(
                        localDate.getMonth().getDisplayName(SHORT_STANDALONE, Locale.ENGLISH),
                        weekEmotions);
            }
        });

        monthLabelToStartMapping
                .forEach((k, v) -> dayChartData.getLabelsDataMap()
                        .put(k, Arrays.stream(v.toArray())
                                .mapToInt(i -> (Integer) i).
                                        average()
                                .orElse(0.0)));

        return dayChartData;
    }

    private StatsDTO calculateActivityStats(List<Day> days) {
        LOGGER.info("Calculating Activity stats from {} days' data", days.size());

        StatsDTO statsDTO = new StatsDTO();
        statsDTO.setDay(calculateActivityStatsForDayByHour(days.get(0)));

        // TODO - make call to get ChartData for week, month, year

        return statsDTO;
    }

    public ChartData calculateActivityStatsForDayByHour(Day day) {
        LOGGER.info("Using latest date {}, to calculate activities by hour", day.getDate());

        ChartData dayChartData = ChartData.dayChartData();

        Set<String> uniqueActivities = new HashSet<>();
        Map<String, List<String>> hourToValueMapping = new HashMap<>();

        dayChartData.getLabels().forEach((hourLabel) -> day.getActivities().forEach((activity) -> {
            if (hourLabel.equals(String.format("%s %s",
                    activity.getStartTime().substring(0, 2), activity.getStartTime().substring(6, 8)))) {
                if (!hourToValueMapping.containsKey(hourLabel)) {
                    hourToValueMapping.put(hourLabel, new ArrayList<>());
                }
                hourToValueMapping.get(hourLabel).add(activity.getName());
                uniqueActivities.add(activity.getName());
            } else {
                hourToValueMapping.put(hourLabel, new ArrayList<>());
            }
        }));

        dayChartData.setLegend(new ArrayList<>(uniqueActivities));

        hourToValueMapping.forEach((k, v) -> {

            List<Integer> activitiesCount;

            if (v.isEmpty()) {
                int[] temp = new int[dayChartData.getLegend().size()];
                Arrays.fill(temp, 0);
                activitiesCount = Arrays.stream(temp).boxed().collect(Collectors.toList());
            } else {
                Map<String, Long> activityCountMap = v.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                long[] temp = new long[dayChartData.getLegend().size()];
                for (int i = 0; i < dayChartData.getLegend().size(); i++) {
                    String activityName = dayChartData.getLegend().get(i);
                    temp[i] = activityCountMap.getOrDefault(activityName, 0L);
                }

                activitiesCount = Arrays.stream(temp).boxed().map(Long::intValue).collect(Collectors.toList());
            }

            dayChartData.getLabelsDataMap().put(k, activitiesCount);
        });

        return dayChartData;
    }

    private StatsDTO calculatePromptStats(List<Day> days) {
        return new StatsDTO();
    }

    private StatsDTO calculateSummaryStats(List<Day> days) {
        return new StatsDTO();
    }
}
