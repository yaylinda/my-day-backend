package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.DayEventCatalog;

public interface DayEventCatalogRepository extends MongoRepository<DayEventCatalog, String> {
}
