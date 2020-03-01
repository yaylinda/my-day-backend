package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import yay.linda.mydaybackend.entity.CatalogEvent;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogEventDTO {
    private String catalogEventId;
    private EventType type; // ACTIVITY or PROMPT
    private String belongsTo;

    // fields for ACTIVITY
    private String name;
    private String description;
    private String icon;
    private Integer count; // for stats

    // fields for PROMPT
    private String question;
    private List<String> answers;
    private List<Integer> answersCount; // for stats

    public CatalogEventDTO(CatalogEvent catalogEvent, Integer count, List<Integer> answersCount) {
        this.catalogEventId = catalogEvent.getCatalogEventId();
        this.type = catalogEvent.getType();
        this.belongsTo = catalogEvent.getBelongsTo();

        this.name = catalogEvent.getName();
        this.description = catalogEvent.getDescription();
        this.icon = catalogEvent.getIcon();
        this.count = count;

        this.question = catalogEvent.getQuestion();
        this.answers = catalogEvent.getAnswers();
        this.answersCount = answersCount;
    }
}
