package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.Day;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface DayRepository extends MongoRepository<Day, String> {

    Optional<Day> findTopByUsernameOrderByDateDesc(String username);

    List<Day> findTop10ByUsernameOrderByDateDesc(String username);
}
