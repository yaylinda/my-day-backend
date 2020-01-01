package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayPromptDTO {
    private String question;
    private List<String> answers;
    private String selectedAnswer;

    private String startTime;
}
