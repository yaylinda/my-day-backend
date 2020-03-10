package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.CatalogEvent;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.DayEventDTO;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.model.TileDataDTO;
import yay.linda.mydaybackend.repository.CatalogEventRepository;
import yay.linda.mydaybackend.repository.DayRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static yay.linda.mydaybackend.model.EventType.ACTIVITY;
import static yay.linda.mydaybackend.model.EventType.EMOTION;
import static yay.linda.mydaybackend.model.EventType.PROMPT;

@Service
public class StatsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class);

    private static final String SCORE_KEY = "Scores";
    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private CatalogEventRepository catalogEventRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AggregationService aggregationService;

    public Map<String, List<TileDataDTO>> getTileStats(String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        List<Day> days = dayRepository.findByUsernameOrderByDateDesc(username);

        List<CatalogEvent> allActivities = catalogEventRepository.findByBelongsToAndType(username, ACTIVITY);

        Map<String, List<TileDataDTO>> tileStats = new HashMap<>();

        tileStats.put(SCORE_KEY, new ArrayList<>());
        allActivities.forEach(a -> tileStats.put(String.format("%s %s", a.getIcon(), a.getName()), new ArrayList<>()));

        days.forEach(d -> {

            // calculate values for score key
            double averageScore = d.getEmotions().stream()
                    .mapToDouble(DayEventDTO::getEmotionScore)
                    .average()
                    .orElse(0.0);

            tileStats.get(SCORE_KEY)
                    .add(new TileDataDTO(
                            d.getDate(),
                            "Average Score",
                            averageScore
                    ));

            // calculate values for-each activity key
            Map<String, Integer> activityCountMap = d.getActivities().stream()
                    .collect(Collectors.groupingBy(DayEventDTO::getName))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().size()
                    ));

            allActivities.forEach(a -> tileStats.get(String.format("%s %s", a.getIcon(), a.getName()))
                    .add(new TileDataDTO(
                            d.getDate(),
                            "Count",
                            activityCountMap.getOrDefault(a.getName(), 0)
                    )));
        });

        return tileStats;
    }

    public Map<String, Number> getSummaryStats(String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);
        List<Day> days = dayRepository.findByUsernameOrderByDateDesc(username);

        Map<String, Number> statsMap = new HashMap<>();
        statsMap.put("totalNumDays", days.size());

        /*
         * Scores summary stats (total count, average)
         */
        statsMap.put("totalNumScores", days.stream().mapToLong(d -> d.getEmotions().size()).sum());
        statsMap.put("averageScore", days.stream()
                .flatMapToDouble(
                        d -> d.getEmotions().stream()
                                .mapToDouble(DayEventDTO::getEmotionScore))
                .average()
                .orElse(0.0));

        /*
         * Activities summary stats (total count, unique count)
         */
        Map<String, List<DayEventDTO>> activitiesById = days.stream()
                .flatMap(d -> d.getActivities().stream())
                .collect(Collectors.groupingBy(DayEventDTO::getCatalogEventId));

        statsMap.put("totalNumActivities", activitiesById.values().stream().mapToLong(List::size).sum());
        statsMap.put("numUniqueActivities", activitiesById.size());

        /*
         * Prompts summary stats (total count, unique count)
         */
        Map<String, List<DayEventDTO>> promptsById = days.stream()
                .flatMap(d -> d.getPrompts().stream())
                .collect(Collectors.groupingBy(DayEventDTO::getCatalogEventId));

        statsMap.put("totalNumPrompts", promptsById.values().stream().mapToLong(List::size).sum());
        statsMap.put("numUniuePrompts", promptsById.size());

        return statsMap;
    }

}
