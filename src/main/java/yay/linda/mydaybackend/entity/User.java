package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.RegisterRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class User {
    @Id
    private String userId; // UUID used in database to track unique user. Used in Session.userId
    private String username; // Friendly display username

    private String password;

    private LocalDateTime joinedDate;
    private LocalDateTime lastActiveDate;

    private Map<String, Object> userConfigurations;

    public User(String username, String password) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.joinedDate = LocalDateTime.now();
        this.lastActiveDate = LocalDateTime.now();
        this.userConfigurations = new HashMap<>();
    }
}
