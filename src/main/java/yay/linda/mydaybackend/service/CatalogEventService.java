package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.CatalogEvent;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.CatalogEventRepository;
import yay.linda.mydaybackend.repository.DayRepository;
import yay.linda.mydaybackend.web.error.NotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CatalogEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogEventService.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CatalogEventRepository catalogEventRepository;

    @Autowired
    private DayRepository dayRepository;

    public Map<String, List<CatalogEvent>> getCatalogEvents(String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Map<String, List<CatalogEvent>> catalogs = new HashMap<>();

        Arrays.stream(EventType.values()).forEach(t -> {
            List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, t);
            LOGGER.info("Found {} CatalogEvent for {} of type {}", list.size(), username, t);
            catalogs.put(t.name(), list);
        });

        return catalogs;
    }

    public List<CatalogEvent> addCatalogEvent(String eventType, CatalogEvent catalogEvent, String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        // TODO - validate eventType, and fields of dayEventCatalogDTO
        catalogEvent.setCatalogEventId(UUID.randomUUID().toString());
        catalogEvent.setType(EventType.valueOf(eventType));
        catalogEvent.setBelongsTo(username);

        catalogEventRepository.save(catalogEvent);
        LOGGER.info("Persisted new CatalogEvent: {}", catalogEvent);

        List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, catalogEvent.getType());

        LOGGER.info("Returning {} CatalogEvent of type {} for {}", list.size(), eventType, username);

        return list;
    }

    public List<CatalogEvent> updateCatalogEvent(String eventType, String catalogEventId, CatalogEvent catalogEvent, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        CatalogEvent existing = catalogEventRepository.findByCatalogEventId(catalogEventId)
                .orElseThrow(() -> NotFoundException.catalogEventNotFound(eventType, catalogEventId));

        switch (existing.getType()) {
            case ACTIVITY:
                // Can only update description of ACTIVITY catalog events
                existing.setIcon(catalogEvent.getIcon());
                existing.setDescription(catalogEvent.getDescription());
                break;
            case PROMPT:
                // Can only update answers of PROMPT catalog events
                existing.setAnswers(catalogEvent.getAnswers());
                break;
            default:
                LOGGER.warn("Attempting to update CatalogEvent of type={}, with id={}. Not allowed.", eventType, catalogEventId);
                break;
        }

        catalogEventRepository.save(existing);
        LOGGER.info("Persisted updated CatalogEvent: {}", catalogEvent);

        List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, existing.getType());

        LOGGER.info("Returning {} CatalogEvent of type {} for {}", list.size(), eventType, username);

        return list;
    }

    public List<CatalogEvent> deleteCatalogEvent(String eventType, String catalogEventId, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        CatalogEvent existing = catalogEventRepository.findByCatalogEventId(catalogEventId)
                .orElseThrow(() -> NotFoundException.catalogEventNotFound(eventType, catalogEventId));

        catalogEventRepository.deleteById(catalogEventId);
        LOGGER.info("Deleted CatalogEvent with id={}", catalogEventId);

        List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, existing.getType());

        LOGGER.info("Returning {} CatalogEvent of type {} for {}", list.size(), eventType, username);

        return list;
    }
}
