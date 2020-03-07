package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.DayEventDTO;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.model.TileDataDTO;
import yay.linda.mydaybackend.repository.DayRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class);

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AggregationService aggregationService;

    public Map<String, List<TileDataDTO>> getTileStats(String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        List<Day> days = dayRepository.findByUsernameOrderByDateDesc(username);

        Map<String, List<TileDataDTO>> tileStats = new HashMap<>();

        days.forEach(d -> {
            double averageScore = d.getEmotions().stream()
                    .mapToDouble(DayEventDTO::getEmotionScore)
                    .average()
                    .orElse(0.0);

            tileStats.putIfAbsent(EventType.EMOTION.name(), new ArrayList<>());
            tileStats.get(EventType.EMOTION.name()).add(new TileDataDTO(d.getDate(), "Average Score", averageScore));

            d.getActivities().stream().collect(Collectors.groupingBy(DayEventDTO::getName)).forEach((k, v) -> {
                tileStats.putIfAbsent(k, new ArrayList<>());
                tileStats.get(k).add(new TileDataDTO(d.getDate(), "Count", v.size()));
            });
        });

        return tileStats;
    }

}
