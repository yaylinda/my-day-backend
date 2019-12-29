package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class DayPromptCatalog {

    @Id
    private String dayPromptCatalogId;
    private String belongsTo;
    private String question;
    private List<String> answers;
    private String selectedAnswer;
}
