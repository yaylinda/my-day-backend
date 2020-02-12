package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.DayActivityDTO;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.model.DayEmotionDTO;
import yay.linda.mydaybackend.model.DayEventDTO;
import yay.linda.mydaybackend.model.DayPromptDTO;
import yay.linda.mydaybackend.model.EventType;
import yay.linda.mydaybackend.repository.DayRepository;
import yay.linda.mydaybackend.web.error.NotFoundException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static yay.linda.mydaybackend.Constants.YEAR_MONTH_DAY_FORMATTER;

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

    public DayDTO updateDay(String dayId, String eventType, DayEventDTO dayEvent, String timezone, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        // TODO - validate eventType

        Optional<Day> optionalDay = dayRepository.findById(dayId);

        if (optionalDay.isPresent()) {
            Day day = optionalDay.get();
            EventType type = EventType.valueOf(eventType.toUpperCase());

            LOGGER.info("Found day with dayId={}", dayId);

            switch (type) {
                case ACTIVITY:
                    DayActivityDTO newActivityDTO = DayActivityDTO.builder()
                            .color(dayEvent.getColor())
                            .description(dayEvent.getDescription())
                            .endTime(dayEvent.getEndTime())
                            .icon(dayEvent.getIcon())
                            .name(dayEvent.getName())
                            .startTime(dayEvent.getStartTime())
                            .timezone(timezone)
                            .build();
                    day.getActivities().add(newActivityDTO);
                    LOGGER.info("Adding ACTIVITY to day");
                    break;
                case EMOTION:
                    DayEmotionDTO newEmotionDTO = DayEmotionDTO.builder()
                            .description(dayEvent.getDescription())
                            .emotionScore(dayEvent.getEmotionScore())
                            .endTime(dayEvent.getEndTime())
                            .startTime(dayEvent.getStartTime())
                            .timezone(timezone)
                            .build();
                    day.getEmotions().add(newEmotionDTO);
                    LOGGER.info("Adding EMOTION to day");
                    break;
                case PROMPT:
                    DayPromptDTO newPromptDTO = DayPromptDTO.builder()
                            .question(dayEvent.getQuestion())
                            .selectedAnswer(dayEvent.getSelectedAnswer())
                            .startTime(dayEvent.getStartTime())
                            .timezone(timezone)
                            .build();
                    day.getPrompts().add(newPromptDTO);
                    LOGGER.info("Adding PROMPT to day");
                    break;
            }

            dayRepository.save(day);
            LOGGER.info("Updated DayEntity for {} with dayId={}, date={}", username, day.getDayId(), day.getDate());

            return new DayDTO(day, true);
        } else {
            throw new NotFoundException(String.format("Day with dayId=%s does not exist for user %s", dayId, username));
        }
    }

    /* THIS METHOD IS NEVER USED */
//    public DayDTO createDay(DayDTO dayDTO, String timezone, String sessionToken) {
//        // TODO - use timezone
//
//        String username = sessionService.getUsernameFromSessionToken(sessionToken);
//
//        // TODO - validate dayDTO: date must not already exist
//
//        if (Objects.isNull(dayDTO.getActivities())) {
//            dayDTO.setActivities(new ArrayList<>());
//        }
//        if (Objects.isNull(dayDTO.getEmotions())) {
//            dayDTO.setEmotions(new ArrayList<>());
//        }
//        if (Objects.isNull(dayDTO.getPrompts())) {
//            dayDTO.setPrompts(new ArrayList<>());
//        }
//
//        Day day = new Day(dayDTO, true);
//        dayRepository.save(day);
//
//        LOGGER.info("Persisted DayEntity for {} with dayId={}, date={}", username, day.getDayId(), day.getDate());
//
//        return dayDTO;
//    }
}
