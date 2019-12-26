package yay.linda.mydaybackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yay.linda.mydaybackend.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
}
