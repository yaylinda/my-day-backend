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
    private List<DayActivityDTO> activities;
    private List<DayPromptDTO> prompts;
    private List<DayEmotionDTO> emotions;

    public DayDTO(Day day, boolean reverse) {
        this.dayId = day.getDayId();
        this.date = day.getDate();
        this.username = day.getUsername();
        this.activities = day.getActivities();
        this.prompts = day.getPrompts();
        this.emotions = day.getEmotions();

        if (reverse) {
            Collections.reverse(this.activities);
            Collections.reverse(this.prompts);
            Collections.reverse(this.emotions);
        }
    }
}
