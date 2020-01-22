package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.ChartData;
import yay.linda.mydaybackend.model.DayEmotionDTO;
import yay.linda.mydaybackend.model.StatsDTO;
import yay.linda.mydaybackend.repository.DayRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.format.TextStyle.SHORT_STANDALONE;
import static yay.linda.mydaybackend.Constants.DAY_KEY;
import static yay.linda.mydaybackend.Constants.HOUR_ORDER;
import static yay.linda.mydaybackend.Constants.MONTHS_ORDER;
import static yay.linda.mydaybackend.Constants.MONTH_DAY_FORMATTER;
import static yay.linda.mydaybackend.Constants.MONTH_KEY;
import static yay.linda.mydaybackend.Constants.WEEK_KEY;
import static yay.linda.mydaybackend.Constants.WEEK_NUM_ORDER;
import static yay.linda.mydaybackend.Constants.YEAR_KEY;
import static yay.linda.mydaybackend.Constants.YEAR_MONTH_DAY_FORMATTER;
import static yay.linda.mydaybackend.Constants.determineWeekStartLabel;
import static yay.linda.mydaybackend.Constants.getLastSevenDays;
import static yay.linda.mydaybackend.Constants.getMonth;
import static yay.linda.mydaybackend.Constants.getYear;

@Service
public class StatsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class);

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AggregationService aggregationService;

    public StatsDTO getStats(String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        List<Day> days = dayRepository.findByUsernameOrderByDateDesc(username);

        return StatsDTO.builder()
                .score(calculateScoreStats(days))
                .activity(calculateActivityStats(days))
                .prompt(calculatePromptStats(days))
                .summary(calculateSummaryStats(days))
                .build();
    }

    private Map<String, ChartData> calculateScoreStats(List<Day> days) {
        LOGGER.info("Calculating Score stats from {} days' data", days.size());

        Map<String, ChartData> scoreStatsMap = new HashMap<>();

        // get stats data for latest day, avg by hour
        scoreStatsMap.put(DAY_KEY, calculateScoreStatsForDayByHour(days.get(0)));

        // get stats data for last 7 days, avg by day
        scoreStatsMap.put(WEEK_KEY, calculateScoreStatsForWeekByDay(getLastSevenDays(days)));

        // get stats data for current month, avg by week number
        scoreStatsMap.put(MONTH_KEY, calculateScoreStatsForMonthByWeek(getMonth(days)));

        // get stats data for current year, avg by month
        scoreStatsMap.put(YEAR_KEY, calculateScoreStatsForYearByMonth(getYear(days)));

        return scoreStatsMap;
    }

    private ChartData calculateScoreStatsForDayByHour(Day day) {
        LOGGER.info("Using latest date [{}], to calculate average scores for DAY (per hour)", day.getDate());

        ChartData<Number> dayChartData = new ChartData<>(HOUR_ORDER);
        Map<String, List<Integer>> hourToValueMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> hourToValueMapping.put(label, new ArrayList<>()));

        day.getEmotions().forEach((emotion) -> {
            String hourLabel = emotion.getStartTime().substring(0, 2) + " " + emotion.getStartTime().substring(6, 8);
            hourToValueMapping.get(hourLabel).add(emotion.getEmotionScore());
        });

        aggregationService.aggregateScoreByLabel(hourToValueMapping, dayChartData);

        return dayChartData;
    }

    private ChartData calculateScoreStatsForWeekByDay(List<Day> week) {
        LOGGER.info("Using dates [{}-{}], to calculate average scores for WEEK (per day)",
                week.get(week.size() - 1), week.get(0));

        List<String> labels = week
                .stream()
                .map(d -> LocalDate.parse(d.getDate()).format(MONTH_DAY_FORMATTER))
                .collect(Collectors.toList());
        Collections.reverse(labels);

        ChartData<Number> dayChartData = new ChartData<>(labels);
        Map<String, List<Integer>> monthDayLabelToValuesMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> monthDayLabelToValuesMapping.put(label, new ArrayList<>()));

        week.forEach(d -> {
            String monthDay = LocalDate.parse(d.getDate()).format(MONTH_DAY_FORMATTER);

            monthDayLabelToValuesMapping.get(monthDay)
                    .addAll(d.getEmotions()
                            .stream()
                            .map(DayEmotionDTO::getEmotionScore)
                            .collect(Collectors.toList()));
        });

        aggregationService.aggregateScoreByLabel(monthDayLabelToValuesMapping, dayChartData);

        return dayChartData;
    }

    private ChartData calculateScoreStatsForMonthByWeek(List<Day> month) {
        LOGGER.info("Using [{}-{}], to calculate average scores for MONTH (per week)",
                month.get(month.size() - 1).getDate(), month.get(0));

        ChartData<Number> dayChartData = new ChartData<>(WEEK_NUM_ORDER);
        Map<String, List<Integer>> weekStartLabelToScoreMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> weekStartLabelToScoreMapping.put(label, new ArrayList<>()));

        month.forEach(d -> weekStartLabelToScoreMapping.get(determineWeekStartLabel(LocalDate.parse(d.getDate())))
                .addAll(d.getEmotions()
                        .stream()
                        .map(DayEmotionDTO::getEmotionScore)
                        .collect(Collectors.toList())));

        aggregationService.aggregateScoreByLabel(weekStartLabelToScoreMapping, dayChartData);

        return dayChartData;
    }

    private ChartData calculateScoreStatsForYearByMonth(List<Day> year) {
        LOGGER.info("Using [{}-{}], to calculate average scores for YEAR (per month)",
                year.get(year.size() - 1).getDate(), year.get(0));

        LocalDate localDate = LocalDate.parse(year.get(0).getDate());
        ChartData<Number> dayChartData = new ChartData<>(MONTHS_ORDER);
        Map<String, List<Integer>> monthLabelToStartMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> monthLabelToStartMapping.put(label, new ArrayList<>()));

        year.forEach(d -> {
            String monthLabel = localDate.getMonth().getDisplayName(SHORT_STANDALONE, Locale.ENGLISH);

            monthLabelToStartMapping.get(monthLabel)
                    .addAll(d.getEmotions()
                            .stream()
                            .map(DayEmotionDTO::getEmotionScore)
                            .collect(Collectors.toList()));
        });

        aggregationService.aggregateScoreByLabel(monthLabelToStartMapping, dayChartData);

        return dayChartData;
    }

    private Map<String, ChartData> calculateActivityStats(List<Day> days) {
        LOGGER.info("Calculating Activity stats from {} days' data", days.size());

        Map<String, ChartData> activityStatsMap = new HashMap<>();

        // get stats data for latest day, avg by hour
        activityStatsMap.put(DAY_KEY, calculateActivityStatsForDayByHour(days.get(0)));

        // get stats data for last 7 days, avg by day
        activityStatsMap.put(WEEK_KEY, calculateActivityStatsForWeekByDay(getLastSevenDays(days)));

        // get stats data for current month, avg by week number
        activityStatsMap.put(MONTH_KEY, calculateActivityStatsForMonthByWeek(getMonth(days)));

        // get stats data for current year, avg by month
        activityStatsMap.put(YEAR_KEY, calculateActivityStatsForYearByMonth(getYear(days)));

        return activityStatsMap;
    }

    public ChartData calculateActivityStatsForDayByHour(Day day) {
        LOGGER.info("Using latest date [{}], to calculate average scores for DAY (per hour)", day.getDate());

        ChartData<List<Number>> dayChartData = new ChartData<>(HOUR_ORDER);
        Map<String, List<String>> hourToValueMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> hourToValueMapping.put(label, new ArrayList<>()));

        Set<String> uniqueActivities = new HashSet<>();
        day.getActivities().forEach((activity) -> {
            String hourLabel = String.format("%s %s", activity.getStartTime().substring(0, 2), activity.getStartTime().substring(6, 8));
            hourToValueMapping.get(hourLabel).add(activity.getName());
            uniqueActivities.add(activity.getName());
        });

        aggregationService.aggregateActivityByLabel(hourToValueMapping, dayChartData, uniqueActivities);

        return dayChartData;
    }

    private ChartData calculateActivityStatsForWeekByDay(List<Day> week) {
        LOGGER.info("Using dates [{}-{}], to calculate average scores for WEEK (per day)",
                week.get(week.size() - 1), week.get(0));

        List<String> labels = week
                .stream()
                .map(d -> LocalDate.parse(d.getDate()).format(MONTH_DAY_FORMATTER))
                .collect(Collectors.toList());
        Collections.reverse(labels);

        ChartData<List<Number>> dayChartData = new ChartData<>(labels);
        Map<String, List<String>> weekdayLabelToValueMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> weekdayLabelToValueMapping.put(label, new ArrayList<>()));

        Set<String> uniqueActivities = new HashSet<>();

        week.forEach(d -> d.getActivities().forEach(a -> {
            String weekdayLabel = LocalDate.parse(d.getDate()).format(MONTH_DAY_FORMATTER);
            weekdayLabelToValueMapping.get(weekdayLabel).add(a.getName());
            uniqueActivities.add(a.getName());
        }));

        aggregationService.aggregateActivityByLabel(weekdayLabelToValueMapping, dayChartData, uniqueActivities);

        return dayChartData;
    }

    private ChartData calculateActivityStatsForMonthByWeek(List<Day> month) {
        LOGGER.info("Using [{}-{}], to calculate average scores for MONTH (per week)",
                month.get(month.size() - 1).getDate(), month.get(0));

        ChartData<List<Number>> dayChartData = new ChartData<>(WEEK_NUM_ORDER);
        Map<String, List<String>> weekStartLabelToScoreMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> weekStartLabelToScoreMapping.put(label, new ArrayList<>()));

        Set<String> uniqueActivities = new HashSet<>();

        month.forEach(d -> d.getActivities().forEach(a -> {
            weekStartLabelToScoreMapping
                    .get(determineWeekStartLabel(LocalDate.parse(d.getDate())))
                    .add(a.getName());
            uniqueActivities.add(a.getName());
        }));

        aggregationService.aggregateActivityByLabel(weekStartLabelToScoreMapping, dayChartData, uniqueActivities);

        return dayChartData;
    }

    private ChartData calculateActivityStatsForYearByMonth(List<Day> year) {
        LOGGER.info("Using [{}-{}], to calculate average scores for YEAR (per month)",
                year.get(year.size() - 1).getDate(), year.get(0));

        ChartData<List<Number>> dayChartData = new ChartData<>(MONTHS_ORDER);
        Map<String, List<String>> monthLabelToStartMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> monthLabelToStartMapping.put(label, new ArrayList<>()));

        Set<String> uniqueActivities = new HashSet<>();

        year.forEach(d -> d.getActivities().forEach(a -> {
            String monthLabel = LocalDate.parse(d.getDate(), YEAR_MONTH_DAY_FORMATTER).getMonth()
                    .getDisplayName(SHORT_STANDALONE, Locale.ENGLISH);
            monthLabelToStartMapping.get(monthLabel).add(a.getName());
            uniqueActivities.add(a.getName());
        }));

        aggregationService.aggregateActivityByLabel(monthLabelToStartMapping, dayChartData, uniqueActivities);

        return dayChartData;
    }

    private Map<String, ChartData> calculatePromptStats(List<Day> days) {
        LOGGER.info("Calculating Prompt stats from {} days' data", days.size());

        Map<String, ChartData> promptStatsMap = new HashMap<>();

        promptStatsMap.put(DAY_KEY, aggregationService.aggregatePromptAnswerStats(Collections.singletonList(days.get(0))));

        promptStatsMap.put(WEEK_KEY, aggregationService.aggregatePromptAnswerStats(getLastSevenDays(days)));

        promptStatsMap.put(MONTH_KEY, aggregationService.aggregatePromptAnswerStats(getMonth(days)));

        promptStatsMap.put(YEAR_KEY, aggregationService.aggregatePromptAnswerStats(getYear(days)));

        return promptStatsMap;
    }

    private Map<String, ChartData> calculateSummaryStats(List<Day> days) {
        // TODO
        return new HashMap<>();
    }
}
