package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.EventType;

@Data
public class DayEventCatalog {

    @Id
    private String dayEventCatalogId; // UUID for a unique DayEventCatalog Entity
    private String belongsTo; // User.id of the user that created this

    private EventType type;
    private String name;
    private String color;
    private String icon;
    private String description;
}
