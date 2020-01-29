package yay.linda.mydaybackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.ChartData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AggregationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class);

    public void aggregateScoreByLabel(Map<String, List<Integer>> map, ChartData chartData) {
        map.forEach((k, v) ->
                chartData.getLabelsDataMap()
                        .put(k, Arrays.stream(v.toArray())
                                .mapToInt(i -> (Integer) i)
                                .average()
                                .orElse(0.0)));
    }

    public void aggregateActivityByLabel(
            Map<String, List<String>> map,
            ChartData<List<Number>> chartData,
            Set<String> uniqueActivities) {

        chartData.setLegend(new ArrayList<>(uniqueActivities));

        map.forEach((k, v) -> {

            List<Number> activitiesCount;

            if (v.isEmpty()) {
                int[] temp = new int[chartData.getLegend().size()];
                Arrays.fill(temp, 0);
                activitiesCount = Arrays.stream(temp).boxed().collect(Collectors.toList());
            } else {
                Map<String, Long> activityCountMap = v.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                long[] temp = new long[chartData.getLegend().size()];
                for (int i = 0; i < chartData.getLegend().size( ); i++) {
                    String activityName = chartData.getLegend().get(i);
                    temp[i] = activityCountMap.getOrDefault(activityName, 0L);
                }

                activitiesCount = Arrays.stream(temp).boxed().map(Long::intValue).collect(Collectors.toList());
            }

            chartData.getLabelsDataMap().put(k, activitiesCount);
        });
    }

    public ChartData<ChartData<Integer>> aggregatePromptAnswerStats(Map<String, Map<String, Integer>> promptsAnswersMap) {
        ChartData<ChartData<Integer>> chartData = new ChartData<>(new ArrayList<>(promptsAnswersMap.keySet()));

        chartData.getLabels().forEach(q -> {
            Map<String, Integer> answerToCountsMap = promptsAnswersMap.get(q);
            ChartData<Integer> innerChartData = new ChartData<>(new ArrayList<>(answerToCountsMap.keySet()));
            innerChartData.setLabelsDataMap(answerToCountsMap);
            chartData.getLabelsDataMap().put(q, innerChartData);
        });

        return chartData;
    }

    public Object getMostCommon(Map<?, Integer> map) {
        Integer mostCommonCount = 0;
        Object mostCommon = null;

        for (Object k : map.keySet()) {
            Integer count = map.get(k);
            if (count > mostCommonCount) {
                mostCommonCount = count;
                mostCommon = k;
            }
        }

        return mostCommon;
    }

    // TODO - aggregation heatmap
}
