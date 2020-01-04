package yay.linda.mydaybackend.service;

import com.sun.jdi.ArrayReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.controller.DayController;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.ChartData;
import yay.linda.mydaybackend.model.DayEmotionDTO;
import yay.linda.mydaybackend.model.StatsDTO;
import yay.linda.mydaybackend.model.StatsType;
import yay.linda.mydaybackend.repository.DayRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        statsDTO.setDay(calculateScoreStatsForDayByHour(days.get(0)));

        // TODO - make call to get ChartData for week, month, year

        return statsDTO;
    }

    private ChartData calculateScoreStatsForDayByHour(Day day) {
        LOGGER.info("Using latest date {}, to calculate average scores by hour", day.getDate());

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
