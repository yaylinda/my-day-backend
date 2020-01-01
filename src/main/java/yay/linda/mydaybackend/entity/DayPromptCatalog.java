package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.CatalogEventDTO;
import yay.linda.mydaybackend.model.DayEventDTO;

import java.util.List;
import java.util.UUID;

@Data
public class DayPromptCatalog extends CatalogEventDTO {

    @Id
    private String dayPromptCatalogId;
    private String belongsTo;

    private String question;
    private List<String> answers;

    public DayPromptCatalog(CatalogEventDTO catalogEventDTO, String username) {
        this.dayPromptCatalogId = UUID.randomUUID().toString();
        this.belongsTo = username;
        this.question = catalogEventDTO.getQuestion();
        this.answers = catalogEventDTO.getAnswers();
    }
}
