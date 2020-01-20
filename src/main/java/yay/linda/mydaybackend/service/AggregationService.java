package yay.linda.mydaybackend.service;

import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.model.ChartData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AggregationService {

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

    // TODO - aggregation heatmap
}
