package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.DayEventDTO;

import java.util.List;

@Data
public class Day {

    @Id
    private String dayId; // UUID for a Day Entity
    private String date; // YYYY-MM-DD format
    private String username; // username that created the Day

    private List<DayEventDTO> events;
}
