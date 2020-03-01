package yay.linda.mydaybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import yay.linda.mydaybackend.entity.CatalogEvent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerCatalogEventDTO {

    // This represents a CatalogEvent (entity), where the type is ANSWER

    private String catalogEventId;
    private String belongsTo;
    private Integer count;

    private String answer; // this is the only field required for input
    private String parentQuestionCatalogEventId;

    public AnswerCatalogEventDTO(CatalogEvent catalogEvent) {
        this.catalogEventId = catalogEvent.getCatalogEventId();
        this.belongsTo = catalogEvent.getBelongsTo();
        this.count = catalogEvent.getCount();
        this.answer = catalogEvent.getAnswer();
        this.parentQuestionCatalogEventId = catalogEvent.getParentQuestionCatalogEventId();
    }
}
