package yay.linda.mydaybackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.AnswerCatalogEventDTO;
import yay.linda.mydaybackend.model.CatalogEventDTO;
import yay.linda.mydaybackend.model.EventType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogEvent {
    @Id
    private String catalogEventId;
    private EventType type; // ACTIVITY or PROMPT or ANSWER
    private String belongsTo;
    private Integer count;

    // fields for ACTIVITY
    private String name;
    private String description;
    private String icon;

    // fields for PROMPT
    private String question;

    // fields for ANSWER
    private String answer;
    private String parentQuestionCatalogEventId;

    private CatalogEvent(CatalogEventDTO catalogEventDTO) {
        this.catalogEventId = catalogEventDTO.getCatalogEventId();
        this.type = catalogEventDTO.getType();
        this.belongsTo = catalogEventDTO.getBelongsTo();
        this.count = catalogEventDTO.getCount();
    }

    public static CatalogEvent createForActivity(CatalogEventDTO catalogEventDTO) {
        CatalogEvent catalogEvent = new CatalogEvent(catalogEventDTO);
        catalogEvent.setName(catalogEventDTO.getName());
        catalogEvent.setDescription(catalogEventDTO.getDescription());
        catalogEvent.setIcon(catalogEventDTO.getIcon());
        return catalogEvent;
    }

    public static CatalogEvent createForPrompt(CatalogEventDTO catalogEventDTO) {
        CatalogEvent catalogEvent = new CatalogEvent(catalogEventDTO);
        catalogEvent.setQuestion(catalogEventDTO.getQuestion());
        return catalogEvent;
    }

    public static CatalogEvent createForAnswer(AnswerCatalogEventDTO answerCatalogEventDTO) {
        CatalogEvent catalogEvent = new CatalogEvent();
        catalogEvent.setCatalogEventId(answerCatalogEventDTO.getCatalogEventId());
        catalogEvent.setType(EventType.ANSWER);
        catalogEvent.setBelongsTo(answerCatalogEventDTO.getBelongsTo());
        catalogEvent.setCount(answerCatalogEventDTO.getCount());
        catalogEvent.setAnswer(answerCatalogEventDTO.getAnswer());
        catalogEvent.setParentQuestionCatalogEventId(answerCatalogEventDTO.getParentQuestionCatalogEventId());
        return catalogEvent;
    }
}
