package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.Day;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class User {
    @Id
    private String id; // UUID used in database to track unique user. Used in Session.userId
    private String username; // Friendly display username
    private Map<String, Day> daysData;
    private LocalDateTime joinedDate;
    private LocalDateTime lastActiveDate;
}
