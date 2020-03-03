package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import yay.linda.mydaybackend.entity.CatalogEvent;
import yay.linda.mydaybackend.model.AnswerCatalogEventDTO;
import yay.linda.mydaybackend.model.CatalogEventDTO;
import yay.linda.mydaybackend.model.CountUpdateType;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.CatalogEventRepository;
import yay.linda.mydaybackend.repository.DayRepository;
import yay.linda.mydaybackend.web.error.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static yay.linda.mydaybackend.model.EventType.ACTIVITY;
import static yay.linda.mydaybackend.model.EventType.ANSWER;
import static yay.linda.mydaybackend.model.EventType.PROMPT;

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

        // Collect all user's CatalogEvents by Type
        Map<EventType, List<CatalogEvent>> catalogEventsByType = catalogEventRepository.findByBelongsTo(username).stream()
                .collect(Collectors.groupingBy(CatalogEvent::getType));

        // Group all user's CatalogEvent Answers by parent question catalogEventId
        Map<String, List<CatalogEvent>> promptIdToAnswersMap = catalogEventsByType.getOrDefault(ANSWER, new ArrayList<>()).stream()
                .collect(Collectors.groupingBy(CatalogEvent::getParentQuestionCatalogEventId));

        Map<String, List<CatalogEventDTO>> catalogs = new HashMap<>();

        catalogs.put(
                ACTIVITY.name(),
                convertActivityCatalogEvents(catalogEventsByType.getOrDefault(ACTIVITY, new ArrayList<>()))
        );

        catalogs.put(
                PROMPT.name(),
                convertPromptCatalogEvents(
                        catalogEventsByType.getOrDefault(PROMPT, new ArrayList<>()),
                        catalogEventsByType.getOrDefault(ANSWER, new ArrayList<>())
                )
        );

        return catalogs;
    }

    public List<CatalogEventDTO> addCatalogEvent(String eventType, CatalogEventDTO catalogEventDTO, String sessionToken) {

        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        catalogEventDTO.setCatalogEventId(UUID.randomUUID().toString());
        catalogEventDTO.setType(EventType.valueOf(eventType));
        catalogEventDTO.setBelongsTo(username);
        catalogEventDTO.setCount(0);

        switch (catalogEventDTO.getType()) {
            case ACTIVITY:
                catalogEventRepository.save(CatalogEvent.createForActivity(catalogEventDTO));
                LOGGER.info("Persisted new ACTIVITY CatalogEvent");
                break;
            case PROMPT:
                List<CatalogEvent> toSave = new ArrayList<>();
                toSave.add(CatalogEvent.createForPrompt(catalogEventDTO));
                catalogEventDTO.getAnswers().forEach(a -> {
                    a.setCatalogEventId(UUID.randomUUID().toString());
                    a.setBelongsTo(username);
                    a.setCount(0);
                    a.setParentQuestionCatalogEventId(catalogEventDTO.getCatalogEventId());
                    toSave.add(CatalogEvent.createForAnswer(a));
                });
                catalogEventRepository.saveAll(toSave);
                LOGGER.info("Persisted new PROMPT CatalogEvent, with {} ANSWERS", catalogEventDTO.getAnswers().size());
                break;
            default:
                LOGGER.warn("Unsupported EventType: {}, for method addCatalogEvent", eventType);
                break;
        }

        return returnCatalogEventsForUser(username, catalogEventDTO.getType());
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
                catalogEventRepository.save(existing);
                LOGGER.info("Persisted updates for ACTIVITY CatalogEvent");
                break;
            case PROMPT:
                // Delete existing
                catalogEventRepository.deleteByParentQuestionCatalogEventId(existing.getCatalogEventId());

                // Can only update answers related to PROMPT catalog events
                List<CatalogEvent> toSave = new ArrayList<>();
                catalogEventDTO.getAnswers().forEach(a -> {
                    if (StringUtils.isEmpty(a.getCatalogEventId())) {
                        a.setCatalogEventId(UUID.randomUUID().toString());
                        a.setBelongsTo(username);
                        a.setCount(0);
                        a.setParentQuestionCatalogEventId(existing.getCatalogEventId());
                    }
                    toSave.add(CatalogEvent.createForAnswer(a));
                });
                catalogEventRepository.saveAll(toSave);
                LOGGER.info("Persisted updates for PROMPT CatalogEvent, with {} ANSWERS", catalogEventDTO.getAnswers().size());
                break;
            default:
                LOGGER.warn("Unsupported EventType: {}, for method updateCatalogEvent", eventType);
                break;
        }

        return returnCatalogEventsForUser(username, existing.getType());
    }

    public List<CatalogEventDTO> deleteCatalogEvent(String eventType, String catalogEventId, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        CatalogEvent existing = catalogEventRepository.findByCatalogEventId(catalogEventId)
                .orElseThrow(() -> NotFoundException.catalogEventNotFound(eventType, catalogEventId));

        catalogEventRepository.deleteById(catalogEventId);
        LOGGER.info("Deleted CatalogEvent with id={}", catalogEventId);

        List<CatalogEvent> list = catalogEventRepository.findByBelongsToAndType(username, existing.getType());

        LOGGER.info("Returning {} CatalogEvent of type {} for {}", list.size(), eventType, username);

        return returnCatalogEventsForUser(username, existing.getType());
    }

    void updateCount(String catalogEventId, EventType eventType, CountUpdateType countUpdateType) {
        CatalogEvent existing = catalogEventRepository.findByCatalogEventId(catalogEventId)
                .orElseThrow(() -> NotFoundException.catalogEventNotFound(eventType.name(), catalogEventId));

        LOGGER.info("{} count for catalogEventId={} of type {}", countUpdateType, catalogEventId, eventType);

        existing.setCount(existing.getCount() + countUpdateType.getAmount());
        catalogEventRepository.save(existing);

        LOGGER.info("Count is now {}", existing.getCount());
    }

    private List<CatalogEventDTO> returnCatalogEventsForUser(String username, EventType type) {
        switch (type) {
            case ACTIVITY:
                return convertActivityCatalogEvents(catalogEventRepository.findByBelongsToAndType(username, ACTIVITY));
            case PROMPT:
                return convertPromptCatalogEvents(
                        catalogEventRepository.findByBelongsToAndType(username, PROMPT),
                        catalogEventRepository.findByBelongsToAndType(username, ANSWER)
                );
            default:
                LOGGER.warn("Unsupported EventType: {}, for method returnCatalogEventsForUser()", type);
                return new ArrayList<>();
        }
    }

    private List<CatalogEventDTO> convertActivityCatalogEvents(List<CatalogEvent> activities) {
        return activities.stream()
                .map(CatalogEventDTO::createForActivity)
                .collect(Collectors.toList());
    }

    private List<CatalogEventDTO> convertPromptCatalogEvents(List<CatalogEvent> prompts, List<CatalogEvent> answers) {
        return prompts.stream()
                .map(p -> CatalogEventDTO.createForPrompt(p, convertAnswerCatalogEvents(answers)))
                .collect(Collectors.toList());
    }

    private List<AnswerCatalogEventDTO> convertAnswerCatalogEvents(List<CatalogEvent> answers) {
        return answers.stream()
                .map(AnswerCatalogEventDTO::new)
                .collect(Collectors.toList());
    }

    /*
    // These methods are not used anymore, since we are storing the counts / usage of catalog events

    private Map<String, Integer> calculateCountsByActivity(String username, List<String> activities) {
        Map<String, Integer> countsByActivity = activities.stream()
                .collect(Collectors.toMap(
                        a -> a,
                        a -> 0
                ));

        dayRepository.findByUsername(username)
                .forEach(d -> d.getActivities()
                        .forEach(a -> countsByActivity.computeIfPresent(a.getName(), (k, v) -> v += 1)));

        return countsByActivity;
    }

    private Map<String, Map<String, Integer>> calculateCountsByPrompt(String username, Map<String, List<String>> promptsAndAnswers) {
        Map<String, Map<String, Integer>> countsByPrompt = promptsAndAnswers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .collect(Collectors.toMap(
                                        a -> a,
                                        a -> 0
                                ))
                ));

        dayRepository.findByUsername(username)
                .forEach(d -> d.getPrompts()
                        .forEach(p -> {
                            if (countsByPrompt.containsKey(p.getQuestion())) {
                                countsByPrompt.get(p.getQuestion())
                                        .computeIfPresent(p.getSelectedAnswer(), (k, v) -> v += 1);
                            }
                        })
                );

        return countsByPrompt;
    }
    */
}
