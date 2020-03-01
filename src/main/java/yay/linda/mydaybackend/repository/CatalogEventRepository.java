package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.CatalogEvent;
import yay.linda.mydaybackend.model.EventType;

import java.util.List;
import java.util.Optional;

public interface CatalogEventRepository extends MongoRepository<CatalogEvent, String> {

    List<CatalogEvent> findByBelongsToAndType(String belongsTo, EventType type);

    Optional<CatalogEvent> findByCatalogEventId(String catalogEventId);
}
