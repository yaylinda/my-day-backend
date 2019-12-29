package yay.linda.mydaybackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yay.linda.mydaybackend.entity.Day;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.repository.DayRepository;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class DayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DayService.class);

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private SessionService sessionService;

    // issue might arrise later where a user will have many Days. we should not have to load the entire list.
    // user query params for pagination
    public List<DayDTO> getDays(String sessionToken) {
        String username = sessionService.getUsernameFromSessionToken(sessionToken);

        List<Day> days = dayRepository.findByUsername(username);
        LOGGER.info("Found {} days for {}", days.size(), username);

        return days.stream().map(DayDTO::new).collect(Collectors.toList());
    }
}
