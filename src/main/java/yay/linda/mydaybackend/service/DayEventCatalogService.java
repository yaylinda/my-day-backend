package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.DayActivityCatalog;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.DayActivityCatalogRepository;
import yay.linda.mydaybackend.repository.DayPromptCatalogRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DayEventCatalogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DayEventCatalogService.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DayActivityCatalogRepository dayActivityCatalogRepository;

    @Autowired
    private DayPromptCatalogRepository dayPromptCatalogRepository;

    public Map<String, List<DayActivityCatalog>> getCatalogs(String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Map<String, List<DayActivityCatalog>> catalogs = new HashMap<>();

        Arrays.stream(EventType.values()).forEach(t -> {
            List<DayActivityCatalog> list = dayActivityCatalogRepository.findByBelongsToAndType(username, t);
            LOGGER.info("Found {} DayEvents for {} of type {}", list.size(), username, t);
            catalogs.put(t.name(), list);
        });

        return catalogs;
    }

    public List<DayActivityCatalog> addDayEvent(String eventType, DayActivityCatalog dayEvent, String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        // TODO - validate eventType, and fields of dayEvent
        // TODO - implement add PROMPT type

        dayEvent.setDayEventCatalogId(UUID.randomUUID().toString());
        dayEvent.setBelongsTo(username);
        dayEvent.setType(EventType.valueOf(eventType));

        dayActivityCatalogRepository.save(dayEvent);
        LOGGER.info("Persisted new DayEvent: {}", dayEvent);

        List<DayActivityCatalog> list = dayActivityCatalogRepository.findByBelongsToAndType(username, dayEvent.getType());
        LOGGER.info("Found {} DayEvents for {} of type {}", list.size(), username, dayEvent.getType());

        return list;
    }

    // TODO - implement update day event
}
