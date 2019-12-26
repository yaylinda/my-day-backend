package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.Day;

public interface DayRepository extends MongoRepository<Day, String> {
}
