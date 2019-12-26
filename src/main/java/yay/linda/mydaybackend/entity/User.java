package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class User {
    @Id
    private String userId; // UUID used in database to track unique user. Used in Session.userId
    private String username; // Friendly display username

    private LocalDateTime joinedDate;
    private LocalDateTime lastActiveDate;

    private Map<String, Object> userConfigurations;
}
