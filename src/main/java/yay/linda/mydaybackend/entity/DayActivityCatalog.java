package yay.linda.mydaybackend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import yay.linda.mydaybackend.model.CatalogEventDTO;
import yay.linda.mydaybackend.model.EventType;

import java.util.UUID;

@Data
public class DayActivityCatalog extends CatalogEventDTO {

    @Id
    private String dayEventCatalogId; // UUID for a unique DayActivityCatalog Entity
    private String belongsTo; // username of the user that created this

    private EventType type;
    private String name;
    private String color;
    private String icon;
    private String description;

    public DayActivityCatalog(CatalogEventDTO catalogEventDTO, String username) {
        this.dayEventCatalogId = UUID.randomUUID().toString();
        this.belongsTo = username;
        this.type = EventType.ACTIVITY;
        this.name = catalogEventDTO.getName();
        this.color = catalogEventDTO.getColor();
        this.icon = catalogEventDTO.getIcon();
        this.description = catalogEventDTO.getDescription();
    }
}
