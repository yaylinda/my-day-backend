package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.DayEvent;

import java.util.List;

@Data
public class Day {

    @Id
    private String dayId; // UUID for a Day Entity
    private String date; // YYYY-MM-DD format
    private String userId; // User.id that created the Day

    private List<DayEvent> events;
}
