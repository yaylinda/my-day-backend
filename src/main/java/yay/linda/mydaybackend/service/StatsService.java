package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.ChartData;
import yay.linda.mydaybackend.model.DayEmotionDTO;
import yay.linda.mydaybackend.model.EventType;
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
import static yay.linda.mydaybackend.Constants.COUNTS_KEY;
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
        LOGGER.info("Calculating Summary stats from {} days' data", days.size());

        // counts
        // total days with any records
        // total days since start of app
        // count of days with scores
        // count of days with activities
        // count of days with prompts
        // count of total scores recorded
        // count of total activities recorded
        // count of total prompts recorded

        // records
        // day with lowest avg score
        // lowest score value
        // day with highest avg score
        // highest score value
        // day with most scores
        // day with most activities
        // day with most prompts
        // most popular score
        // most popular activity
        // most answered prompt
        // answer distribution of most answered prompt - TODO - calculate using aggregatePromptAnswerStats(days)

        // Variable to accumulate stats
        int daysSinceStart = days.size();
        int daysRecorded = 0;
        int daysWithScores = 0;
        int daysWithActivities = 0;
        int daysWithPrompts = 0;
        int numScores = 0;
        int numActivities = 0;
        int numPrompts = 0;
        double lowestAvgDayScore = 5.0;
        String dateOfLowestScore = "";
        double highestAvgDayScore = 0.0;
        String dateOfHighestScore = "";
        Map<String, Map<EventType, Integer>> dateToEventCountMap = new HashMap<>();
        Map<Integer, Integer> scoreCountMap = new HashMap<>();
        Map<String, Integer> activityCountMap = new HashMap<>();

        // Go through days and do accumulation
        for (Day d : days) {

            // count days where any event is recorded
            if (!CollectionUtils.isEmpty(d.getEmotions())
                    && !CollectionUtils.isEmpty(d.getActivities())
                    && !CollectionUtils.isEmpty(d.getPrompts())) {
                daysRecorded += 1;
            }

            // count days where one type of event is recorded
            daysWithScores += CollectionUtils.isEmpty(d.getEmotions()) ? 0 : 1;
            daysWithActivities += CollectionUtils.isEmpty(d.getActivities()) ? 0 : 1;
            daysWithPrompts += CollectionUtils.isEmpty(d.getPrompts()) ? 0 : 1;

            // count event types recorded
            numScores += d.getEmotions().size();
            numActivities += d.getActivities().size();
            numPrompts += d.getPrompts().size();

            // avg day score
            double avgScore = d.getEmotions().stream()
                    .map(DayEmotionDTO::getEmotionScore)
                    .reduce((a, b) -> a + b)
                    .orElse(0) * 1.0 / d.getEmotions().size();

            if (avgScore <= lowestAvgDayScore) {
                lowestAvgDayScore = avgScore;
                dateOfLowestScore = d.getDate();
            }

            if (avgScore >= highestAvgDayScore) {
                highestAvgDayScore = avgScore;
                dateOfHighestScore = d.getDate();
            }

            // accumulate counts in maps

            dateToEventCountMap.put(d.getDate(), Map.of(
                    EventType.EMOTION, d.getEmotions().size(),
                    EventType.ACTIVITY, d.getActivities().size(),
                    EventType.PROMPT, d.getPrompts().size()));

            d.getEmotions().forEach(e -> {
                scoreCountMap.putIfAbsent(e.getEmotionScore(), 0);
                scoreCountMap.put(e.getEmotionScore(), scoreCountMap.get(e.getEmotionScore()) + 1);
            });

            d.getActivities().forEach(a -> {
                activityCountMap.putIfAbsent(a.getName(), 0);
                activityCountMap.put(a.getName(), activityCountMap.get(a.getName()) + 1);
            });

        }

        Map<String, ChartData> summaryStatsMap = new HashMap<>();

        return summaryStatsMap;
    }
}
