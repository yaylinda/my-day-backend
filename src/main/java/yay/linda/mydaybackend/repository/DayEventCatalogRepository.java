package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.DayEventCatalog;
import yay.linda.mydaybackend.model.EventType;

import java.util.List;

public interface DayEventCatalogRepository extends MongoRepository<DayEventCatalog, String> {

    List<DayEventCatalog> findByBelongsToAndType(String belongsTo, EventType type);
}
