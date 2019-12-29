package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.Day;

import java.util.List;

public interface DayRepository extends MongoRepository<Day, String> {

    List<Day> findByUsername(String username);
}
