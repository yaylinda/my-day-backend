package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.ChartData;
import yay.linda.mydaybackend.model.DayEmotionDTO;
import yay.linda.mydaybackend.model.StatsDTO;
import yay.linda.mydaybackend.model.StatsType;
import yay.linda.mydaybackend.repository.DayRepository;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.format.TextStyle.SHORT_STANDALONE;
import static yay.linda.mydaybackend.Constants.YEAR_MONTH_DAY_FORMATTER;
import static yay.linda.mydaybackend.Constants.determineWeekStartLabel;
import static yay.linda.mydaybackend.Constants.getMonth;
import static yay.linda.mydaybackend.Constants.getWeek;
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

    public StatsDTO getStats(String statsType, String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        List<Day> days = dayRepository.findByUsernameOrderByDateDesc(username);

        StatsType type = StatsType.valueOf(statsType.toUpperCase());

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
        statsDTO.setWeek(calculateScoreStatsForWeekByDay(getWeek(days)));

        // get stats data for current month, avg by week number
        statsDTO.setMonth(calculateScoreStatsForMonthByWeek(getMonth(days)));

        // get stats data for current year, avg by month
        statsDTO.setYear(calculateScoreStatsForYearByMonth(getYear(days)));

        return statsDTO;
    }

    private ChartData calculateScoreStatsForDayByHour(Day day) {
        LOGGER.info("Using latest date [{}], to calculate average scores for DAY (per hour)", day.getDate());

        ChartData dayChartData = ChartData.dayChartData();
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

        ChartData dayChartData = ChartData.weekChartData();
        Map<String, List<Integer>> weekdayLabelToScoreMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> weekdayLabelToScoreMapping.put(label, new ArrayList<>()));

        week.forEach(d -> {
            String weekday = LocalDate.parse(d.getDate())
                    .getDayOfWeek().getDisplayName(SHORT_STANDALONE, Locale.ENGLISH);

            weekdayLabelToScoreMapping.get(weekday)
                    .addAll(d.getEmotions()
                            .stream()
                            .map(DayEmotionDTO::getEmotionScore)
                            .collect(Collectors.toList()));
        });

        aggregationService.aggregateScoreByLabel(weekdayLabelToScoreMapping, dayChartData);

        return dayChartData;
    }

    private ChartData calculateScoreStatsForMonthByWeek(List<Day> month) {
        LOGGER.info("Using [{}-{}], to calculate average scores for MONTH (per week)",
                month.get(month.size() - 1).getDate(), month.get(0));

        ChartData dayChartData = ChartData.monthChartData();
        Map<String, List<Integer>> weekStartLabelToScoreMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> weekStartLabelToScoreMapping.put(label, new ArrayList<>()));

        month.forEach(d -> weekStartLabelToScoreMapping.get(determineWeekStartLabel(LocalDate.parse(month.get(0).getDate())))
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
        ChartData dayChartData = ChartData.yearChartData();
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

    private StatsDTO calculateActivityStats(List<Day> days) {
        LOGGER.info("Calculating Activity stats from {} days' data", days.size());

        StatsDTO statsDTO = new StatsDTO();

        // get stats data for latest day, avg by hour
        statsDTO.setDay(calculateActivityStatsForDayByHour(days.get(0)));

        // get stats data for last 7 days, avg by day
        statsDTO.setWeek(calculateActivityStatsForWeekByDay(getWeek(days)));

        // get stats data for current month, avg by week number
        statsDTO.setMonth(calculateActivityStatsForMonthByWeek(getMonth(days)));

        // get stats data for current year, avg by month
        statsDTO.setYear(calculateActivityStatsForYearByMonth(getYear(days)));

        return statsDTO;
    }

    public ChartData calculateActivityStatsForDayByHour(Day day) {
        LOGGER.info("Using latest date [{}], to calculate average scores for DAY (per hour)", day.getDate());

        ChartData dayChartData = ChartData.dayChartData();
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

        ChartData dayChartData = ChartData.weekChartData();
        Map<String, List<String>> weekdayLabelToValueMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> weekdayLabelToValueMapping.put(label, new ArrayList<>()));

        Set<String> uniqueActivities = new HashSet<>();

        week.forEach(d -> d.getActivities().forEach(a -> {
            String weekdayLabel = LocalDate.parse(d.getDate(), YEAR_MONTH_DAY_FORMATTER)
                    .getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH);
            weekdayLabelToValueMapping.get(weekdayLabel).add(a.getName());
            uniqueActivities.add(a.getName());
        }));

        aggregationService.aggregateActivityByLabel(weekdayLabelToValueMapping, dayChartData, uniqueActivities);

        return dayChartData;
    }

    private ChartData calculateActivityStatsForMonthByWeek(List<Day> month) {
        LOGGER.info("Using [{}-{}], to calculate average scores for MONTH (per week)",
                month.get(month.size() - 1).getDate(), month.get(0));

        ChartData dayChartData = ChartData.monthChartData();
        Map<String, List<String>> weekStartLabelToScoreMapping = new HashMap<>();
        dayChartData.getLabels().forEach((label) -> weekStartLabelToScoreMapping.put(label, new ArrayList<>()));

        Set<String> uniqueActivities = new HashSet<>();

        month.forEach(d -> d.getActivities().forEach(a -> {
            weekStartLabelToScoreMapping
                    .get(determineWeekStartLabel(LocalDate.parse(month.get(0).getDate())))
                    .add(a.getName());
            uniqueActivities.add(a.getName());
        }));

        aggregationService.aggregateActivityByLabel(weekStartLabelToScoreMapping, dayChartData, uniqueActivities);

        return dayChartData;
    }

    private ChartData calculateActivityStatsForYearByMonth(List<Day> year) {
        LOGGER.info("Using [{}-{}], to calculate average scores for YEAR (per month)",
                year.get(year.size() - 1).getDate(), year.get(0));

        ChartData dayChartData = ChartData.yearChartData();
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


    private StatsDTO calculatePromptStats(List<Day> days) {
        return new StatsDTO();
    }

    private StatsDTO calculateSummaryStats(List<Day> days) {
        return new StatsDTO();
    }
}
