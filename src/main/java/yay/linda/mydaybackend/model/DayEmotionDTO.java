package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEmotionDTO {
    private Integer emotionScore;
    private String description;

    private String startTime; // HH:MM (A/P)M
    private String endTime;
}
