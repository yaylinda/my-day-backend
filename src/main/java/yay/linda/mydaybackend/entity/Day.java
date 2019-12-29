package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.model.DayEventDTO;
import yay.linda.mydaybackend.model.DayPromptDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static yay.linda.mydaybackend.Constants.YEAR_MONTH_DAY_FORMATTER;

@Data
public class Day {

    @Id
    private String dayId; // UUID for a Day Entity
    private String date; // YYYY-MM-DD format
    private String username; // username that created the Day

    private List<DayEventDTO> events;
    private List<DayPromptDTO> prompts;

    public Day(DayDTO dayDTO, boolean isNew) {
        if (isNew) {
            dayDTO.setDayId(UUID.randomUUID().toString());
        }

        this.dayId = dayDTO.getDayId();
        this.date = dayDTO.getDate();
        this.username = dayDTO.getUsername();
        this.events = dayDTO.getEvents();
        this.prompts = dayDTO.getPrompts();
    }

    public Day(String date, String username) {
        this.dayId = UUID.randomUUID().toString();
        this.date = date;
        this.username = username;
        this.events = new ArrayList<>();
        this.prompts = new ArrayList<>();
    }
}
