package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class Session {
    @Id
    private String id; // UUID representing sessionToken id
    private String userId; // UUID of a unique User. Linked to User.id
    private Date createdDate;
    private Boolean isActive;
}
