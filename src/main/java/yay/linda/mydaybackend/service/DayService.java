package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.model.DayEmotionDTO;
import yay.linda.mydaybackend.repository.DayRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static yay.linda.mydaybackend.Constants.YEAR_MONTH_DAY_FORMATTER;

@Service
public class DayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DayService.class);

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private SessionService sessionService;

    // issue might arrise later where a user will have many Days. we should not have to load the entire list.
    // user query params for pagination
    // for now, limit to 10 days
    public List<DayDTO> getDays(String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        Optional<Day> optionalDay = dayRepository.findTopByUsernameOrderByDateDesc(username);

        LocalDate today = LocalDate.now();

        if (optionalDay.isPresent()) {
            LOGGER.info("Found latest Day data");

            if (!optionalDay.get().getDate().equals(today.format(YEAR_MONTH_DAY_FORMATTER))) {
                LOGGER.info("Latest Day date {} does not equal today's date {}. Catching up...",
                        optionalDay.get().getDate(), today.format(YEAR_MONTH_DAY_FORMATTER));

                LocalDate latest = LocalDate.parse(optionalDay.get().getDate(), YEAR_MONTH_DAY_FORMATTER);

                List<Day> daysToSave = new ArrayList<>();

                while (latest.isBefore(today)) {
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

        return days.stream().map(DayDTO::new).collect(Collectors.toList());
    }

    public DayDTO createDay(DayDTO dayDTO, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        // TODO - validate dayDTO: date must not exist

        Day day = new Day(dayDTO, true);
        dayRepository.save(day);

        LOGGER.info("Persisted DayEntity for {} with dayId={}, date={}", username, day.getDayId(), day.getDate());

        return dayDTO;
    }

    public DayDTO updateDay(String dayId, String eventType, DayEmotionDTO dayEmotionDTO, String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        // TODO - validate dayDTO: dayId and date must exist

        Day day = new Day(dayDTO, false);
        dayRepository.save(day);

        LOGGER.info("Updated DayEntity for {} with dayId={}, date={}", username, day.getDayId(), day.getDate());

        return dayDTO;
    }
}
