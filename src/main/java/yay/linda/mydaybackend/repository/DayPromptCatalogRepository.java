package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.DayActivityCatalog;
import yay.linda.mydaybackend.entity.DayPromptCatalog;
import yay.linda.mydaybackend.model.EventType;

import java.util.List;

public interface DayPromptCatalogRepository extends MongoRepository<DayPromptCatalog, String> {

    List<DayPromptCatalog> findByBelongsTo(String belongsTo);

}
