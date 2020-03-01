package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import yay.linda.mydaybackend.entity.CatalogEvent;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogEventDTO {

    private String catalogEventId;
    private EventType type; // ACTIVITY or PROMPT
    private String belongsTo;
    private Integer count; // for stats

    // fields for ACTIVITY
    private String name;
    private String description;
    private String icon;

    // fields for PROMPT
    private String question;
    private List<AnswerCatalogEventDTO> answers;

    private CatalogEventDTO(CatalogEvent catalogEvent) {
        this.catalogEventId = catalogEvent.getCatalogEventId();
        this.type = catalogEvent.getType();
        this.belongsTo = catalogEvent.getBelongsTo();
        this.count = catalogEvent.getCount();
    }

    public static CatalogEventDTO createForActivity(CatalogEvent catalogEvent) {
        CatalogEventDTO catalogEventDTO = new CatalogEventDTO(catalogEvent);
        catalogEventDTO.setName(catalogEvent.getName());
        catalogEventDTO.setDescription(catalogEvent.getDescription());
        catalogEventDTO.setIcon(catalogEvent.getIcon());
        return catalogEventDTO;
    }

    public static CatalogEventDTO createForPrompt(CatalogEvent catalogEvent, List<AnswerCatalogEventDTO> answers) {
        CatalogEventDTO catalogEventDTO = new CatalogEventDTO(catalogEvent);
        catalogEventDTO.setQuestion(catalogEvent.getQuestion());
        catalogEventDTO.setAnswers(answers);
        return catalogEventDTO;
    }
}
