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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.time.format.TextStyle.SHORT_STANDALONE;
import static yay.linda.mydaybackend.Constants.COUNTS_KEY;
import static yay.linda.mydaybackend.Constants.DAY_KEY;
import static yay.linda.mydaybackend.Constants.HOUR_ORDER;
import static yay.linda.mydaybackend.Constants.MONTHS_ORDER;
import static yay.linda.mydaybackend.Constants.MONTH_DAY_FORMATTER;
import static yay.linda.mydaybackend.Constants.MONTH_KEY;
import static yay.linda.mydaybackend.Constants.RECORDS_KEY;
import static yay.linda.mydaybackend.Constants.WEEK_KEY;
import static yay.linda.mydaybackend.Constants.WEEK_NUM_ORDER;
import static yay.linda.mydaybackend.Constants.YEAR_KEY;
import static yay.linda.mydaybackend.Constants.YEAR_MONTH_DAY_FORMATTER;
import static yay.linda.mydaybackend.Constants.determineWeekStartLabel;
import static yay.linda.mydaybackend.Constants.getLastSevenDays;
import static yay.linda.mydaybackend.Constants.getMonth;
import static yay.linda.mydaybackend.Constants.getYear;
import static yay.linda.mydaybackend.model.EventType.ACTIVITY;
import static yay.linda.mydaybackend.model.EventType.EMOTION;
import static yay.linda.mydaybackend.model.EventType.PROMPT;

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

        promptStatsMap.put(DAY_KEY, aggregationService.aggregatePromptAnswerStats(
                collectPromptAnswerStats(Collections.singletonList(days.get(0)))));

        promptStatsMap.put(WEEK_KEY, aggregationService.aggregatePromptAnswerStats(
                collectPromptAnswerStats(getLastSevenDays(days))));

        promptStatsMap.put(MONTH_KEY, aggregationService.aggregatePromptAnswerStats(
                collectPromptAnswerStats(getMonth(days))));

        promptStatsMap.put(YEAR_KEY, aggregationService.aggregatePromptAnswerStats(
                collectPromptAnswerStats(getYear(days))));

        return promptStatsMap;
    }

    private Map<String, Map<String, Integer>> collectPromptAnswerStats(List<Day> days) {
        LOGGER.info("Using [{}-{}], to collect prompt stats for each answer choice",
                days.get(days.size() - 1).getDate(), days.get(0));

        Map<String, Map<String, Integer>> promptsAnswersMap = new HashMap<>();

        days.forEach(d -> d.getPrompts().forEach(p -> {
            promptsAnswersMap.putIfAbsent(p.getQuestion(), new HashMap<>());
            promptsAnswersMap.get(p.getQuestion()).putIfAbsent(p.getSelectedAnswer(), 0);
            promptsAnswersMap.get(p.getQuestion()).put(
                    p.getSelectedAnswer(),
                    promptsAnswersMap.get(p.getQuestion()).get(p.getSelectedAnswer()) + 1);
        }));

        return promptsAnswersMap;
    }

    private Map<String, ChartData> calculateSummaryStats(List<Day> days) {
        LOGGER.info("Calculating Summary stats from {} days' data", days.size());

        // Variable to accumulate stats
        int numDaysTotal = days.size();
        int numDaysWithRecords = 0;
        int numDaysWithScore = 0;
        int numDaysWithActivity = 0;
        int numDaysWithPrompt = 0;
        int numScoresTotal = 0;
        int numActivitiesTotal = 0;
        int numPromptsTotal = 0;

        double lowestAvgDayScore = 5.0;
        String lowestAvgDayScoreDate = "";
        double highestAvgDayScore = 0.0;
        String highestAvgDayScoreDate = "";

        Map<String, Map<EventType, Integer>> dateToEventCountMap = new HashMap<>();
        Map<Integer, Integer> scoreCountMap = new HashMap<>();
        Map<String, Integer> activityCountMap = new HashMap<>();
        Map<String, Integer> promptCountMap = collectPromptAnswerStats(days).entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().values()
                                .stream()
                                .reduce((a, b) -> a + b)
                                .orElse(0)));

        // Go through days and do accumulation
        for (Day d : days) {

            // count days where any event is recorded
            if (!CollectionUtils.isEmpty(d.getEmotions())
                    || !CollectionUtils.isEmpty(d.getActivities())
                    || !CollectionUtils.isEmpty(d.getPrompts())) {
                numDaysWithRecords += 1;
            }

            // count days where one type of event is recorded
            numDaysWithScore += CollectionUtils.isEmpty(d.getEmotions()) ? 0 : 1;
            numDaysWithActivity += CollectionUtils.isEmpty(d.getActivities()) ? 0 : 1;
            numDaysWithPrompt += CollectionUtils.isEmpty(d.getPrompts()) ? 0 : 1;

            // count event types recorded
            numScoresTotal += d.getEmotions().size();
            numActivitiesTotal += d.getActivities().size();
            numPromptsTotal += d.getPrompts().size();

            // avg day score
            double avgScore = d.getEmotions().stream()
                    .map(DayEmotionDTO::getEmotionScore)
                    .reduce((a, b) -> a + b)
                    .orElse(0) * 1.0 / d.getEmotions().size();

            if (avgScore <= lowestAvgDayScore) {
                lowestAvgDayScore = avgScore;
                lowestAvgDayScoreDate = d.getDate();
            }

            if (avgScore >= highestAvgDayScore) {
                highestAvgDayScore = avgScore;
                highestAvgDayScoreDate = d.getDate();
            }

            // accumulate counts in maps

            dateToEventCountMap.put(d.getDate(), Map.of(
                    EMOTION, d.getEmotions().size(),
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

        // Set stats in summary stats map
        Map<String, ChartData> summaryStatsMap = new HashMap<>();

        // Set Count stats
        ChartData<Integer> countsChartData = new ChartData<>();
        countsChartData.setLabelsDataMap(Map.of(
                "numDaysTotal", numDaysTotal,
                "numDaysWithRecords", numDaysWithRecords,
                "numDaysWithScore", numDaysWithScore,
                "numDaysWithActivity", numDaysWithActivity,
                "numDaysWithPrompt", numDaysWithPrompt,
                "numScoresTotal", numScoresTotal,
                "numActivitiesTotal", numActivitiesTotal,
                "numPromptsTotal", numPromptsTotal
        ));
        countsChartData.setLabelsFromDataMap();
        summaryStatsMap.put(COUNTS_KEY, countsChartData);

        // Accumulate Maps for record counts
        String mostScoresDate = "";
        int mostScoresPerDayValue = 0;

        String mostActivitiesDate = "";
        int mostActivitiesPerDayValue = 0;

        String mostPromptsDate = "";
        int mostPromptsPerDayValue = 0;

        for (String date : dateToEventCountMap.keySet()) {
            Map<EventType, Integer> value = dateToEventCountMap.get(date);
            if (value.get(EMOTION) > mostScoresPerDayValue) {
                mostScoresDate = date;
                mostScoresPerDayValue = value.get(EMOTION);
            }
            if (value.get(ACTIVITY) > mostActivitiesPerDayValue) {
                mostActivitiesDate = date;
                mostActivitiesPerDayValue = value.get(ACTIVITY);
            }
            if (value.get(PROMPT) > mostPromptsPerDayValue) {
                mostPromptsDate = date;
                mostPromptsPerDayValue = value.get(PROMPT);
            }
        }

        // Find most common score
        Integer mostCommonScore = (Integer) aggregationService.getMostCommon(scoreCountMap);
        Integer mostCommonScoreCount = scoreCountMap.get(mostCommonScore);

        // Find most common activity
        String mostCommonActivity = (String) aggregationService.getMostCommon(activityCountMap);
        Integer mostCommonActivityCount = activityCountMap.get(mostCommonActivity);

        // Find most common prompt
        String mostCommonPrompt = (String) aggregationService.getMostCommon(promptCountMap);
        Integer mostCommonPromptCount = promptCountMap.get(mostCommonPrompt);

        // Set Records stats
        ChartData<Object> recordsChartData = new ChartData<>();
        recordsChartData.setLabelsDataMap(Map.of(
                "lowestAvgDayScore", lowestAvgDayScore,
                "lowestAvgDayScoreDate", lowestAvgDayScoreDate,
                "highestAvgDayScore", highestAvgDayScore,
                "highestAvgDayScoreDate", highestAvgDayScoreDate,
//                "mostScoresDate", mostScoresDate,
//                "mostScoresPerDayValue", mostScoresPerDayValue,
//                "mostActivitiesDate", mostActivitiesDate,
//                "mostActivitiesPerDayValue", mostActivitiesPerDayValue,
//                "mostPromptsDate", mostPromptsDate,
//                "mostPromptsPerDayValue", mostPromptsPerDayValue,
                "mostCommonScore", mostCommonScore,
                "mostCommonScoreCount", mostCommonScoreCount,
                "mostCommonActivity", mostCommonActivity,
                "mostCommonActivityCount", mostCommonActivityCount,
                "mostCommonPrompt", mostCommonPrompt,
                "mostCommonPromptCount", mostCommonPromptCount
        ));

        recordsChartData.setLabelsFromDataMap();
        summaryStatsMap.put(RECORDS_KEY, recordsChartData);

        return summaryStatsMap;
    }
}
