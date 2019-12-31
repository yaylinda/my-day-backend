package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.EventType;

@Data
public class DayActivityCatalog {

    @Id
    private String dayEventCatalogId; // UUID for a unique DayActivityCatalog Entity
    private String belongsTo; // username of the user that created this

    private EventType type;
    private String name;
    private String color;
    private String icon;
    private String description;
}
