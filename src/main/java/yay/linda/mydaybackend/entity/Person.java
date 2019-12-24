package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.Day;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class Person {
    @Id
    private String id;
    private String name;
    private Map<String, Day> daysData;
    private LocalDateTime joinedDate;
    private LocalDateTime lastActiveDate;
}
