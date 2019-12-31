package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayActivityDTO {
    private String name;
    private String color;
    private String icon;
    private String description;

    private String startTime;
    private String endTime;
}
