package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayPromptDTO extends DayEventDTO {
    // TODO - try to consolidate and extent DayEventDTO

    private String dayEventId;
    private String question;
    private String selectedAnswer;

    private String startTime;
    private String timezone;
}
