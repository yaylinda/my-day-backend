package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.Session;

public interface SessionRepository extends MongoRepository<Session, String> {
}
