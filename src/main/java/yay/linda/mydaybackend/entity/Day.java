package yay.linda.mydaybackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.DayDTO;
import yay.linda.mydaybackend.model.DayEventDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Day {

    @Id
    private String dayId; // UUID for a Day Entity
    private String date; // YYYY-MM-DD format
    private String username; // username that created the Day

    private List<DayEventDTO> activities;
    private List<DayEventDTO> prompts;
    private List<DayEventDTO> emotions;

    public Day(DayDTO dayDTO, boolean isNew) {
        if (isNew) {
            dayDTO.setDayId(UUID.randomUUID().toString());
        }

        this.dayId = dayDTO.getDayId();
        this.date = dayDTO.getDate();
        this.username = dayDTO.getUsername();
        this.activities = dayDTO.getActivities();
        this.prompts = dayDTO.getPrompts();
        this.emotions = dayDTO.getEmotions();
    }

    public Day(String date, String username) {
        this.dayId = UUID.randomUUID().toString();
        this.date = date;
        this.username = username;
        this.activities = new ArrayList<>();
        this.prompts = new ArrayList<>();
        this.emotions = new ArrayList<>();
    }
}
