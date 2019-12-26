package yay.linda.mydaybackend.model;

import lombok.Data;

@Data
public class DayEventDTO {
    private EventType type;
    private String name;
    private String color;
    private String icon;
    private String description;
}
