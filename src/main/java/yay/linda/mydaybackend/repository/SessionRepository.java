package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.Session;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends MongoRepository<Session, String> {

    Optional<Session> findBySessionToken(String sessionToken);

    List<Session> findByUsernameAndIsActive(String username, Boolean isActive);

    void deleteBySessionToken(String sessionToken);
}
