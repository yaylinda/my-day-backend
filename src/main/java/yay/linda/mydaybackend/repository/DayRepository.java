package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import yay.linda.mydaybackend.entity.Day;

import java.util.List;
import java.util.Optional;

public interface DayRepository extends MongoRepository<Day, String> {

    Optional<Day> findTopByUsernameOrderByDateDesc(String username);

    List<Day> findTop10ByUsernameOrderByDateDesc(String username);

    List<Day> findByUsernameOrderByDateDesc(String username);
}
