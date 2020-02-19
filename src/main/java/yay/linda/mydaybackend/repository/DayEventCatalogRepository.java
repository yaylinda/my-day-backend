package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.DayEventCatalog;
import yay.linda.mydaybackend.model.EventType;

import java.util.List;
import java.util.Optional;

public interface DayEventCatalogRepository extends MongoRepository<DayEventCatalog, String> {

    List<DayEventCatalog> findByBelongsToAndType(String belongsTo, EventType type);

    Optional<DayEventCatalog> findByCatalogEventId(String catalogEventId);
}
