package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.DayActivityCatalog;
import yay.linda.mydaybackend.entity.DayPromptCatalog;
import yay.linda.mydaybackend.model.CatalogEventDTO;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.DayActivityCatalogRepository;
import yay.linda.mydaybackend.repository.DayPromptCatalogRepository;

import java.util.ArrayList;
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

    public Map<String, List<? extends CatalogEventDTO>> getCatalogEvents(String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Map<String, List<? extends CatalogEventDTO>> catalogs = new HashMap<>();

        Arrays.stream(EventType.values()).forEach(t -> {
            switch (t) {
                case ACTIVITY:
                    List<? extends CatalogEventDTO> activitiesList = dayActivityCatalogRepository.findByBelongsToAndType(username, t);
                    LOGGER.info("Found {} DayActivityCatalogs for {} of type {}", activitiesList.size(), username, t);
                    catalogs.put(t.name(), activitiesList);
                case EMOTION:
                    // no op
                case PROMPT:
                    List<? extends CatalogEventDTO> promptsList = dayPromptCatalogRepository.findByBelongsTo(username);
                    LOGGER.info("Found {} DayPromptCatalogs for {}", promptsList.size(), username);
                    catalogs.put(t.name(), promptsList);
                    break;
            }
        });

        return catalogs;
    }

    public List<? extends CatalogEventDTO> addCatalogEvent(String eventType, CatalogEventDTO catalogEventDTO, String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        // TODO - validate eventType, and fields of catalogEventDTO
        // TODO - implement add PROMPT type

        List<? extends CatalogEventDTO> list = new ArrayList<>();

        switch (EventType.valueOf(eventType)) {
            case ACTIVITY:
                DayActivityCatalog dayActivityCatalog = new DayActivityCatalog(catalogEventDTO, username);
                dayActivityCatalogRepository.save(dayActivityCatalog);
                LOGGER.info("Persisted new DayActivityCatalog: {}", dayActivityCatalog);
                list = dayActivityCatalogRepository.findByBelongsToAndType(username, EventType.ACTIVITY);
                break;
            case EMOTION:
                // no op
            case PROMPT:
                DayPromptCatalog dayPromptCatalog = new DayPromptCatalog(catalogEventDTO, username);
                dayPromptCatalogRepository.save(dayPromptCatalog);
                LOGGER.info("Persisted new DayPromptCatalog: {}", dayPromptCatalog);
                list = dayPromptCatalogRepository.findByBelongsTo(username);
                break;
        }

        LOGGER.info("Returning {} CatalogEventDTOs of type {} for {}", list.size(), eventType, username);

        return list;
    }

    // TODO - implement update day event
}
