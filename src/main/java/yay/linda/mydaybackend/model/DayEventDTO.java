package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayEventDTO {
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
}
