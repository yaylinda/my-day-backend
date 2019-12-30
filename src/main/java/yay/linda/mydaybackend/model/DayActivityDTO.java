package yay.linda.mydaybackend.model;

import lombok.Data;

@Data
public class DayActivityDTO extends DayActivitiesDTO {
    private String name;
    private String color;
    private String icon;
    private String description;

    private String startTime;
    private String endTime;
}
