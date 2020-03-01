package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

import static yay.linda.mydaybackend.Constants.TIME_FORMATTER;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayEventDTO implements Comparable<DayEventDTO> {

    // fields for DayEmotionDTO
    private Integer emotionScore;

    // fields for DayActivityDTO
    private String name;
    private String color;
    private String description;
    private String icon;

    // fields for DayPromptDTO
    private String question;
    private String selectedAnswer;
    private String selectedAnswerCatalogEventId;

    // fields for all three
    private EventType type;
    private String startTime; // TODO: we are HEAVILY assuming that the format is `hh:mm a`. Maybe make this LocalTime
    private String dayEventId;
    private String timezone;

    // fields for DayEmotionDTO and DayPromptDTO
    private String catalogEventId;

    @Override
    public int compareTo(DayEventDTO o) {
        // Reverse order sort so that most recent events come first. PM before AM.
        LocalTime thisStartTime = LocalTime.parse(this.startTime, TIME_FORMATTER);
        LocalTime otherStartTime = LocalTime.parse(o.startTime, TIME_FORMATTER);
        return thisStartTime.compareTo(otherStartTime) * (-1);
    }
}
