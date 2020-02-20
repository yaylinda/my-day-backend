package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DayEventDTO {
    // TODO - try to consolidate and have this as superclass for the other 3 classes
    private EventType type;

    // fields for DayActivityDTO
    private String name;
    private String color;
    private String icon;

    // fields for DayEmotionDTO
    private Integer emotionScore;

    // fields for both
    private String description;
    private String endTime;

    // fields for DayPromptDTO
    private String question;
    private List<String> answers;
    private String selectedAnswer;

    // fields for all three
    private String startTime;
    private String dayEventId;
}
