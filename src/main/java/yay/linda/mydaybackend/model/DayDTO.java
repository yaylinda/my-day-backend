package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import yay.linda.mydaybackend.entity.Day;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayDTO {

    private String dayId; // UUID for a Day Entity
    private String date; // YYYY-MM-DD format
    private String username; // username that created the Day
    private List<DayEventDTO> activities;
    private List<DayEventDTO> prompts;
    private List<DayEventDTO> emotions;

    public DayDTO(Day day) {
        this.dayId = day.getDayId();
        this.date = day.getDate();
        this.username = day.getUsername();
        this.activities = day.getActivities();
        this.prompts = day.getPrompts();
        this.emotions = day.getEmotions();
        Collections.sort(this.activities);
        Collections.sort(this.prompts);
        Collections.sort(this.emotions);
    }
}
