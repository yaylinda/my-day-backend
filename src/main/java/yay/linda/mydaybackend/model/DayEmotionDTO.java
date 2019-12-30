package yay.linda.mydaybackend.model;

import lombok.Data;

@Data
public class DayEmotionDTO extends DayActivitiesDTO {
    private Integer emotionScore;
    private String description;

    private String startTime;
    private String endTime;
}
