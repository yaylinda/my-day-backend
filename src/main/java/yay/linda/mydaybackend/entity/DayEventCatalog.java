package yay.linda.mydaybackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.EventType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayEventCatalog {

    @Id
    private String catalogEventId;
    private EventType type;
    private String belongsTo;

    // fields for ActivityCatalogDTO
    private String name;
    private String description;
    private String icon;
    private String color;

    // fields for PromptCatalogDTO
    private String question;
    private List<String> answers;
}
