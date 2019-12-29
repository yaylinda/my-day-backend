package yay.linda.mydaybackend.model;

import lombok.Data;
import yay.linda.mydaybackend.entity.Day;

import java.util.List;

@Data
public class DayDTO {

    private String dayId; // UUID for a Day Entity
    private String date; // YYYY-MM-DD format
    private String username; // username that created the Day
    private List<DayEventDTO> events;
    private List<DayPromptDTO> prompts;

    public DayDTO(Day day) {

    }
}
