package yay.linda.mydaybackend.service;

import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.CatalogEvent;
import yay.linda.mydaybackend.model.CatalogEventDTO;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.CatalogEventRepository;
import yay.linda.mydaybackend.repository.DayRepository;
import yay.linda.mydaybackend.web.error.NotFoundException;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CatalogEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogEventService.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CatalogEventRepository catalogEventRepository;

    @Autowired
    private DayRepository dayRepository;

    public Map<String, List<CatalogEventDTO>> getCatalogEvents(String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Map<String, List<CatalogEventDTO>> catalogs = new HashMap<>();

        Arrays.stream(EventType.values()).forEach(t -> {
            List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, t);
            LOGGER.info("Found {} CatalogEvent for {} of type {}", list.size(), username, t);
            catalogs.put(t.name(), list.stream()
                    .map(this::convertAndGetCounts)
                    .collect(Collectors.toList()));
        });

        return catalogs;
    }

    public List<CatalogEventDTO> addCatalogEvent(String eventType, CatalogEventDTO catalogEventDTO, String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        // TODO - validate eventType, and fields of dayEventCatalogDTO
        catalogEventDTO.setCatalogEventId(UUID.randomUUID().toString());
        catalogEventDTO.setType(EventType.valueOf(eventType));
        catalogEventDTO.setBelongsTo(username);

        catalogEventRepository.save(new CatalogEvent(catalogEventDTO));
        LOGGER.info("Persisted new CatalogEvent: {}", catalogEventDTO);

        List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, catalogEventDTO.getType());

        LOGGER.info("Returning {} CatalogEvent of type {} for {}", list.size(), eventType, username);

        return list.stream()
                .map(this::convertAndGetCounts)
                .collect(Collectors.toList());
    }

    public List<CatalogEventDTO> updateCatalogEvent(String eventType, String catalogEventId, CatalogEventDTO catalogEventDTO, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        CatalogEvent existing = catalogEventRepository.findByCatalogEventId(catalogEventId)
                .orElseThrow(() -> NotFoundException.catalogEventNotFound(eventType, catalogEventId));

        switch (existing.getType()) {
            case ACTIVITY:
                // Can only update description of ACTIVITY catalog events
                existing.setIcon(catalogEventDTO.getIcon());
                existing.setDescription(catalogEventDTO.getDescription());
                break;
            case PROMPT:
                // Can only update answers of PROMPT catalog events
                existing.setAnswers(catalogEventDTO.getAnswers());
                break;
            default:
                LOGGER.warn("Attempting to update CatalogEvent of type={}, with id={}. Not allowed.", eventType, catalogEventId);
                break;
        }

        catalogEventRepository.save(existing);
        LOGGER.info("Persisted updated CatalogEvent: {}", catalogEventDTO);

        List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, existing.getType());

        LOGGER.info("Returning {} CatalogEvent of type {} for {}", list.size(), eventType, username);

        return list.stream()
                .map(this::convertAndGetCounts)
                .collect(Collectors.toList());
    }

    public List<CatalogEventDTO> deleteCatalogEvent(String eventType, String catalogEventId, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        CatalogEvent existing = catalogEventRepository.findByCatalogEventId(catalogEventId)
                .orElseThrow(() -> NotFoundException.catalogEventNotFound(eventType, catalogEventId));

        catalogEventRepository.deleteById(catalogEventId);
        LOGGER.info("Deleted CatalogEvent with id={}", catalogEventId);

        List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, existing.getType());

        LOGGER.info("Returning {} CatalogEvent of type {} for {}", list.size(), eventType, username);

        return list.stream()
                .map(this::convertAndGetCounts)
                .collect(Collectors.toList());
    }

    private CatalogEventDTO convertAndGetCounts(CatalogEvent catalogEvent) {
        return new CatalogEventDTO(catalogEvent, getActivityCounts(catalogEvent), getAnswersCounts(catalogEvent));
    }

    private Integer getActivityCounts(CatalogEvent catalogEvent) {
        if (!EventType.ACTIVITY.equals(catalogEvent.getType())) {
            return null;
        }

        return 0; // TODO - calculation
    }

    private List<Integer> getAnswersCounts(CatalogEvent catalogEvent) {
        if (!EventType.PROMPT.equals(catalogEvent.getType())) {
            return null;
        }

        return new ArrayList<>(); // TODO - calculation
    }
}
