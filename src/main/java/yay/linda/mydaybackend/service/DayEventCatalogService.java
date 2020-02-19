package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.DayEventCatalog;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.DayEventCatalogRepository;
import yay.linda.mydaybackend.web.error.NotFoundException;

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
    private DayEventCatalogRepository dayEventCatalogRepository;

    public Map<String, List<DayEventCatalog>> getCatalogEvents(String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Map<String, List<DayEventCatalog>> catalogs = new HashMap<>();

        Arrays.stream(EventType.values()).forEach(t -> {
            List<DayEventCatalog> list = dayEventCatalogRepository.findByBelongsToAndType(username, t);
            LOGGER.info("Found {} DayEventCatalog for {} of type {}", list.size(), username, t);
            catalogs.put(t.name(), list);
        });

        return catalogs;
    }

    public List<DayEventCatalog> addCatalogEvent(String eventType, DayEventCatalog dayEventCatalog, String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        // TODO - validate eventType, and fields of dayEventCatalogDTO
        dayEventCatalog.setCatalogEventId(UUID.randomUUID().toString());
        dayEventCatalog.setType(EventType.valueOf(eventType));
        dayEventCatalog.setBelongsTo(username);

        dayEventCatalogRepository.save(dayEventCatalog);
        LOGGER.info("Persisted new DayEventCatalog: {}", dayEventCatalog);

        List<DayEventCatalog> list = dayEventCatalogRepository.findByBelongsToAndType(username, dayEventCatalog.getType());

        LOGGER.info("Returning {} DayEventCatalog of type {} for {}", list.size(), eventType, username);

        return list;
    }

    public List<DayEventCatalog> updateCatalogEvent(String eventType, String catalogEventId, DayEventCatalog dayEventCatalog, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        DayEventCatalog existing = dayEventCatalogRepository.findByCatalogEventId(catalogEventId)
                .orElseThrow(() -> NotFoundException.catalogEventNotFound(eventType, catalogEventId));

        switch (existing.getType()) {
            case ACTIVITY:
                // Can only update description of ACTIVITY catalog events
                existing.setDescription(dayEventCatalog.getDescription());
                break;
            case PROMPT:
                // Can only update answers of PROMPT catalog events
                existing.setAnswers(dayEventCatalog.getAnswers());
                break;
            default:
                LOGGER.warn("Attempting to update CatalogEvent of type={}, with id={}. Not allowed.", eventType, catalogEventId);
                break;
        }

        dayEventCatalogRepository.save(existing);
        LOGGER.info("Persisted updated DayEventCatalog: {}", dayEventCatalog);

        List<DayEventCatalog> list = dayEventCatalogRepository.findByBelongsToAndType(username, existing.getType());

        LOGGER.info("Returning {} DayEventCatalog of type {} for {}", list.size(), eventType, username);

        return list;
    }

    public List<DayEventCatalog> deleteCatalogEvent(String eventType, String catalogEventId, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        DayEventCatalog existing = dayEventCatalogRepository.findByCatalogEventId(catalogEventId)
                .orElseThrow(() -> NotFoundException.catalogEventNotFound(eventType, catalogEventId));

        dayEventCatalogRepository.deleteById(catalogEventId);
        LOGGER.info("Deleted DayEventCatalog with id={}", catalogEventId);

        List<DayEventCatalog> list = dayEventCatalogRepository.findByBelongsToAndType(username, existing.getType());

        LOGGER.info("Returning {} DayEventCatalog of type {} for {}", list.size(), eventType, username);

        return list;
    }
}
