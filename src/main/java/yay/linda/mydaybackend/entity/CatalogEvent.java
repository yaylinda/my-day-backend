package yay.linda.mydaybackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.CatalogEventDTO;
import yay.linda.mydaybackend.model.EventType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogEvent {
    @Id
    private String catalogEventId;
    private EventType type; // ACTIVITY or PROMPT
    private String belongsTo;

    // fields for ACTIVITY
    private String name;
    private String description;
    private String icon;

    // fields for PROMPT
    private String question;
    private List<String> answers;

    public CatalogEvent(CatalogEventDTO catalogEventDTO) {
        this.catalogEventId = catalogEventDTO.getCatalogEventId();
        this.type = catalogEventDTO.getType();
        this.belongsTo = catalogEventDTO.getBelongsTo();
        this.name = catalogEventDTO.getName();
        this.description = catalogEventDTO.getDescription();
        this.icon = catalogEventDTO.getIcon();
        this.question = catalogEventDTO.getQuestion();
        this.answers = catalogEventDTO.getAnswers();
    }
}
