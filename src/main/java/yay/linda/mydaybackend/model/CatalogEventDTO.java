package yay.linda.mydaybackend.model;

import lombok.Data;

import java.util.List;

@Data
public class CatalogEventDTO {

    private String catalogEventId;
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
