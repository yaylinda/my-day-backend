package yay.linda.mydaybackend.model;

import lombok.Data;

@Data
public class DayEmotionDTO extends DayEventDTO {
    private Integer emotionScore;
    private String description;

    private String startTime;
    private String endTime;
}
