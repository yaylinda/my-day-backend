package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
public class Session {
    @Id
    private String sessionToken; // UUID representing unique session
    private String username;

    private LocalDateTime createdDate;
    private Boolean isActive;

    public Session(String username) {
        this.sessionToken = UUID.randomUUID().toString();
        this.username = username;
        this.createdDate = LocalDateTime.now();
        this.isActive = true;
    }
}
