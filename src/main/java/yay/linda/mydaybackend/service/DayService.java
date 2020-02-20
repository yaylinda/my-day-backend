package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.model.DayEventDTO;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.DayRepository;
import yay.linda.mydaybackend.web.error.NotFoundException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static yay.linda.mydaybackend.Constants.YEAR_MONTH_DAY_FORMATTER;
import static yay.linda.mydaybackend.model.EventType.ACTIVITY;
import static yay.linda.mydaybackend.model.EventType.EMOTION;
import static yay.linda.mydaybackend.model.EventType.PROMPT;

@Service
public class DayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DayService.class);

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private SessionService sessionService;

    // issue might arise later where a user will have many Days. we should not have to load the entire list.
    // user query params for pagination
    // for now, limit to 10 days
    public List<DayDTO> getDays(String timezone, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Optional<Day> optionalDay = dayRepository.findTopByUsernameOrderByDateDesc(username);

        ZonedDateTime today = ZonedDateTime.now(ZoneId.of(timezone));

        if (optionalDay.isPresent()) {
            LOGGER.info("Found latest Day data");

            if (!optionalDay.get().getDate().equals(today.format(YEAR_MONTH_DAY_FORMATTER))) {
                LOGGER.info("Latest Day date {} does not equal today's date {}. Catching up...",
                        optionalDay.get().getDate(), today.format(YEAR_MONTH_DAY_FORMATTER));

                LocalDate latest = LocalDate.parse(optionalDay.get().getDate(), YEAR_MONTH_DAY_FORMATTER);

                List<Day> daysToSave = new ArrayList<>();

                while (latest.isBefore(today.toLocalDate())) {
                    latest = latest.plusDays(1);
                    LOGGER.info("Saving placeholder Day for date={}", latest.format(YEAR_MONTH_DAY_FORMATTER));
                    daysToSave.add(new Day(latest.format(YEAR_MONTH_DAY_FORMATTER), username));
                }

                dayRepository.saveAll(daysToSave);
            } else {
                LOGGER.info("Latest Day date is today");
            }
        } else {
            LOGGER.info("No Day data. Saving placeholder Day for today={}", today.format(YEAR_MONTH_DAY_FORMATTER));

            Day day = new Day(today.format(YEAR_MONTH_DAY_FORMATTER), username);
            dayRepository.save(day);
        }

        List<Day> days = dayRepository.findTop10ByUsernameOrderByDateDesc(username);

        LOGGER.info("Found {} days for {}", days.size(), username);

        return days.stream().map(d -> new DayDTO(d, true)).collect(Collectors.toList());
    }

    public DayDTO updateDayAddEvent(String dayId, String eventType, DayEventDTO dayEvent, String timezone, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Day day = dayRepository.findById(dayId).orElseThrow(() -> NotFoundException.dayNotFound(dayId, username));

        switch (EventType.valueOf(eventType.toUpperCase())) {
            case ACTIVITY:
                DayEventDTO newActivityDTO = DayEventDTO.builder()
                        .type(ACTIVITY)
                        .color(dayEvent.getColor())
                        .description(dayEvent.getDescription())
                        .endTime(dayEvent.getEndTime())
                        .icon(dayEvent.getIcon())
                        .name(dayEvent.getName())
                        .startTime(dayEvent.getStartTime())
                        .timezone(timezone)
                        .dayEventId(UUID.randomUUID().toString())
                        .build();
                day.getActivities().add(newActivityDTO);
                LOGGER.info("Adding ACTIVITY to day");
                break;
            case EMOTION:
                DayEventDTO newEmotionDTO = DayEventDTO.builder()
                        .type(EMOTION)
                        .description(dayEvent.getDescription())
                        .emotionScore(dayEvent.getEmotionScore())
                        .endTime(dayEvent.getEndTime())
                        .startTime(dayEvent.getStartTime())
                        .timezone(timezone)
                        .dayEventId(UUID.randomUUID().toString())
                        .build();
                day.getEmotions().add(newEmotionDTO);
                LOGGER.info("Adding EMOTION to day");
                break;
            case PROMPT:
                DayEventDTO newPromptDTO = DayEventDTO.builder()
                        .type(PROMPT)
                        .question(dayEvent.getQuestion())
                        .selectedAnswer(dayEvent.getSelectedAnswer())
                        .startTime(dayEvent.getStartTime())
                        .timezone(timezone)
                        .dayEventId(UUID.randomUUID().toString())
                        .build();
                day.getPrompts().add(newPromptDTO);
                LOGGER.info("Adding PROMPT to day");
                break;
        }

        dayRepository.save(day);
        LOGGER.info("Updated DayEntity and added/updated {} event, for {} with dayId={}, date={}",
                eventType, username, day.getDayId(), day.getDate());

        return new DayDTO(day, true);
    }

    public DayDTO updateDayUpdateEvent(String dayId, String eventType, String dayEventId, DayEventDTO dayEvent, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Day day = dayRepository.findById(dayId).orElseThrow(() -> NotFoundException.dayNotFound(dayId, username));

        switch (EventType.valueOf(eventType.toUpperCase())) {
            case ACTIVITY:
                day.getActivities().forEach(a -> {
                    if (a.getDayEventId().equalsIgnoreCase(dayEventId)) {
                        // When updating an ACTIVITY, only time of activity can be changed
                        a.setStartTime(dayEvent.getStartTime());
                    }
                });
                break;
            case EMOTION:
                day.getEmotions().forEach(e -> {
                    if (e.getDayEventId().equalsIgnoreCase(dayEventId)) {
                        // When updating an EMOTION, only time and score can be changed
                        e.setStartTime(dayEvent.getStartTime());
                        e.setEmotionScore(dayEvent.getEmotionScore());
                    }
                });
                break;
            case PROMPT:
                day.getPrompts().forEach(p -> {
                    if (p.getDayEventId().equalsIgnoreCase(dayEventId)) {
                        p.setStartTime(dayEvent.getStartTime());
                        p.setSelectedAnswer(dayEvent.getSelectedAnswer());
                    }
                });
                break;
        }

        dayRepository.save(day);
        LOGGER.info("Updated DayEntity and added/updated {} event, for {} with dayId={}, date={}",
                eventType, username, day.getDayId(), day.getDate());

        return new DayDTO(day, true);
    }

    public DayDTO updateDayDeleteEvent(String dayId, String eventType, String dayEventId, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Day day = dayRepository.findById(dayId).orElseThrow(() -> NotFoundException.dayNotFound(dayId, username));

        int index;

        switch (EventType.valueOf(eventType.toUpperCase())) {
            case ACTIVITY:
                index = findDayEventIndexById(dayEventId, day.getActivities());
                if (index > -1) {
                    day.getActivities().remove(index);
                }
                break;
            case EMOTION:
                index = findDayEventIndexById(dayEventId, day.getEmotions());
                if (index > -1) {
                    day.getEmotions().remove(index);
                }
                break;
            case PROMPT:
                index = findDayEventIndexById(dayEventId, day.getPrompts());
                if (index > -1) {
                    day.getPrompts().remove(index);
                }
                break;
        }

        return new DayDTO(day, true);
    }

    private int findDayEventIndexById(String dayEventId, List<? extends DayEventDTO> dayEvents) {
        int index = -1;
        for (int i = 0; i < dayEvents.size(); i++) {
            DayEventDTO dayEvent = dayEvents.get(i);
            if (dayEvent.getDayEventId().equalsIgnoreCase(dayEventId)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
