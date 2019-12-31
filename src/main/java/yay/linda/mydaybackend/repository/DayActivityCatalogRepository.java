package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.DayActivityCatalog;
import yay.linda.mydaybackend.model.EventType;

import java.util.List;

public interface DayActivityCatalogRepository extends MongoRepository<DayActivityCatalog, String> {

    List<DayActivityCatalog> findByBelongsToAndType(String belongsTo, EventType type);
}
